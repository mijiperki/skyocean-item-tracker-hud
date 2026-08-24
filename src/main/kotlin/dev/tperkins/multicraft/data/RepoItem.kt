package dev.tperkins.multicraft.data

/**
 * A single SkyBlock item as parsed from the upstream data repo.
 *
 * The upstream format is the NotEnoughUpdates (NEU) item schema, which is what
 * meowdding-repo / SkyOcean ultimately consume. We deliberately depend on that
 * open (MIT / Creative-Commons-ish) data rather than on SkyOcean's code.
 *
 * This type is pure JVM and has no Minecraft dependency so the recipe engine can
 * be built and unit-tested without the game.
 */
data class RepoItem(
    /** Stable SkyBlock internal id, e.g. "ENCHANTED_IRON". */
    val internalName: String,
    /** Colour-coded display name, e.g. "§aEnchanted Iron Ingot". */
    val displayName: String,
    /** All known recipes that produce this item (may be empty for raw materials). */
    val recipes: List<Recipe>,
) {
    /** The recipe we will expand in the craft tree, preferring plain crafting. */
    fun primaryCraftingRecipe(): Recipe? =
        recipes.firstOrNull { it.type.equals("crafting", ignoreCase = true) }
            ?: recipes.firstOrNull()

    /** Strip Minecraft formatting codes (§x) for plain-text contexts. */
    val plainName: String
        get() = displayName.replace(Regex("§."), "")
}
