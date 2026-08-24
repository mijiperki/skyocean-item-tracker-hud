package dev.tperkins.multicraft.craft

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.tperkins.multicraft.data.RecipeResolver
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Ordered, persistent list of active [CraftTarget]s. This is the entire "multi"
 * mechanism: SkyOcean keeps one active recipe, we keep N.
 *
 * Adding resolves the tree via [RecipeResolver]; removing/reordering are plain
 * list ops. Targets survive relog via [save]/[load] (we persist inputs only and
 * re-resolve, so a repo update is picked up automatically).
 */
class CraftTargetManager(
    private val resolver: RecipeResolver,
    private val savePath: Path,
) {
    private val targets = ArrayList<CraftTarget>()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun all(): List<CraftTarget> = targets.toList()
    fun isEmpty(): Boolean = targets.isEmpty()
    fun size(): Int = targets.size

    /** Add (or, if already present, update the amount of) a target. Returns it. */
    fun add(internalName: String, amount: Int): CraftTarget {
        val key = internalName.uppercase()
        val existing = targets.firstOrNull { it.internalName == key }
        return if (existing != null) {
            setAmount(existing, amount)
            existing
        } else {
            val target = CraftTarget(key, amount, resolver.resolve(key, amount))
            targets.add(target)
            save()
            target
        }
    }

    fun setAmount(target: CraftTarget, amount: Int) {
        if (amount <= 0) {
            remove(target.internalName)
            return
        }
        target.desiredAmount = amount
        target.tree = resolver.resolve(target.internalName, amount)
        save()
    }

    fun remove(internalName: String): Boolean {
        val removed = targets.removeAll { it.internalName == internalName.uppercase() }
        if (removed) save()
        return removed
    }

    fun clear() {
        targets.clear()
        save()
    }

    /** Move a target up (negative delta) or down (positive) in the list. */
    fun move(internalName: String, delta: Int): Boolean {
        val idx = targets.indexOfFirst { it.internalName == internalName.uppercase() }
        if (idx < 0) return false
        val dest = (idx + delta).coerceIn(0, targets.size - 1)
        if (dest == idx) return false
        val target = targets.removeAt(idx)
        targets.add(dest, target)
        save()
        return true
    }

    fun toggleCollapsed(internalName: String): Boolean {
        val t = targets.firstOrNull { it.internalName == internalName.uppercase() } ?: return false
        t.collapsed = !t.collapsed
        save()
        return true
    }

    // --- Persistence ---

    fun save() {
        val data = targets.map { it.toPersisted() }
        Files.createDirectories(savePath.parent)
        Files.newBufferedWriter(savePath).use { gson.toJson(data, it) }
    }

    fun load() {
        if (!savePath.exists()) return
        val type = object : TypeToken<List<CraftTarget.Persisted>>() {}.type
        val data: List<CraftTarget.Persisted> =
            Files.newBufferedReader(savePath).use { gson.fromJson(it, type) } ?: return
        targets.clear()
        for (p in data) {
            val amount = p.desiredAmount.coerceAtLeast(1)
            targets.add(CraftTarget(p.internalName, amount, resolver.resolve(p.internalName, amount), p.collapsed))
        }
    }
}
