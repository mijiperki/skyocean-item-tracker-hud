package dev.tperkins.multicraft.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

/**
 * In-memory index of SkyBlock items keyed by internal name.
 *
 * Data source strategy (see DESIGN.md §6): we consume the open NEU item schema,
 * the same data meowdding-repo / SkyOcean rely on, instead of coupling to
 * SkyOcean itself. Two on-disk layouts are supported:
 *
 *  - a single JSON object of {internalName -> itemObject} (our bundled snapshot), and
 *  - a directory of per-item `<INTERNALNAME>.json` files (the raw NEU repo layout).
 */
class ItemRepository private constructor(
    private val items: Map<String, RepoItem>,
) {
    val size: Int get() = items.size

    fun item(internalName: String): RepoItem? = items[internalName.uppercase()]

    fun hasCraftingRecipe(internalName: String): Boolean =
        item(internalName)?.primaryCraftingRecipe() != null

    /** Case/format-insensitive prefix search for command autocompletion. */
    fun search(query: String, limit: Int = 25): List<RepoItem> {
        val q = query.uppercase()
        return items.values.asSequence()
            .filter { it.internalName.contains(q) }
            .sortedBy { it.internalName }
            .take(limit)
            .toList()
    }

    companion object {
        private val GRID_KEYS: List<String> =
            listOf("A", "B", "C").flatMap { r -> listOf("1", "2", "3").map { c -> "$r$c" } }

        /** Load from a single JSON object of {internalName -> itemObject}. */
        fun fromItemsObject(root: JsonObject): ItemRepository {
            val map = HashMap<String, RepoItem>()
            for ((key, value) in root.entrySet()) {
                if (!value.isJsonObject) continue
                val item = parseItem(value.asJsonObject, fallbackName = key) ?: continue
                map[item.internalName.uppercase()] = item
            }
            return ItemRepository(map)
        }

        fun fromReader(reader: Reader): ItemRepository =
            fromItemsObject(JsonParser.parseReader(reader).asJsonObject)

        fun fromJson(json: String): ItemRepository =
            fromItemsObject(JsonParser.parseString(json).asJsonObject)

        /** Load a directory of per-item `<NAME>.json` files (raw NEU layout). */
        fun fromDirectory(dir: Path): ItemRepository {
            val map = HashMap<String, RepoItem>()
            Files.newDirectoryStream(dir, "*.json").use { stream ->
                for (file in stream) {
                    if (file.extension != "json") continue
                    val json = Files.newBufferedReader(file).use { JsonParser.parseReader(it) }
                    if (!json.isJsonObject) continue
                    val item = parseItem(json.asJsonObject, fallbackName = file.nameWithoutExtension)
                        ?: continue
                    map[item.internalName.uppercase()] = item
                }
            }
            return ItemRepository(map)
        }

        fun merge(vararg repos: ItemRepository): ItemRepository {
            val map = HashMap<String, RepoItem>()
            for (repo in repos) map.putAll(repo.items)
            return ItemRepository(map)
        }

        /** Parse one NEU-schema item object into a [RepoItem]; null if unusable. */
        fun parseItem(obj: JsonObject, fallbackName: String? = null): RepoItem? {
            val internal = obj.stringOrNull("internalname") ?: fallbackName ?: return null
            val display = obj.stringOrNull("displayname") ?: internal
            val recipes = parseRecipes(obj)
            return RepoItem(internalName = internal, displayName = display, recipes = recipes)
        }

        private fun parseRecipes(obj: JsonObject): List<Recipe> {
            val out = ArrayList<Recipe>()
            // Newer NEU items use a "recipes" array; older ones a single "recipe".
            obj["recipes"]?.let { el ->
                if (el.isJsonArray) el.asJsonArray.forEach { r ->
                    if (r.isJsonObject) parseSingleRecipe(r.asJsonObject)?.let(out::add)
                }
            }
            obj["recipe"]?.let { el ->
                if (el.isJsonObject) parseSingleRecipe(el.asJsonObject)?.let(out::add)
            }
            return out
        }

        private fun parseSingleRecipe(obj: JsonObject): Recipe? {
            val type = obj.stringOrNull("type") ?: "crafting"
            val outputCount = obj["count"]?.takeIf { it.isJsonPrimitive }?.asInt ?: 1
            val ingredients = HashMap<String, Int>()
            for (slot in GRID_KEYS) {
                val raw = obj.stringOrNull(slot)?.takeIf { it.isNotBlank() } ?: continue
                val (name, qty) = parseIngredient(raw) ?: continue
                ingredients.merge(name, qty, Int::plus)
            }
            if (ingredients.isEmpty()) return null
            return Recipe(type = type, outputCount = outputCount.coerceAtLeast(1), ingredients = ingredients)
        }

        /** "ENCHANTED_IRON:32" -> ("ENCHANTED_IRON", 32); missing qty defaults to 1. */
        private fun parseIngredient(raw: String): Pair<String, Int>? {
            val idx = raw.lastIndexOf(':')
            if (idx < 0) return raw to 1
            val name = raw.substring(0, idx)
            val qty = raw.substring(idx + 1).toIntOrNull() ?: 1
            if (name.isBlank()) return null
            return name to qty
        }

        private fun JsonObject.stringOrNull(key: String): String? {
            val el: JsonElement? = this[key]
            return if (el != null && el.isJsonPrimitive) el.asString else null
        }
    }
}
