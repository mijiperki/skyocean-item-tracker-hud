package dev.tperkins.multicraft.craft

import dev.tperkins.multicraft.data.RecipeNode

/**
 * One tracked craft goal — the equivalent of SkyOcean's single active Craft Helper
 * item. The whole point of this mod is that many of these can exist at once
 * (see [CraftTargetManager]); each is fully independent.
 */
class CraftTarget(
    val internalName: String,
    var desiredAmount: Int,
    /** Resolved recipe tree; recomputed when [desiredAmount] changes. */
    var tree: RecipeNode,
    /** UI state: collapsed shows only the header row. */
    var collapsed: Boolean = false,
) {
    val displayName: String get() = tree.displayName

    /** Persisted form — we store only the inputs and re-resolve the tree on load. */
    data class Persisted(
        val internalName: String,
        val desiredAmount: Int,
        val collapsed: Boolean,
    )

    fun toPersisted() = Persisted(internalName, desiredAmount, collapsed)
}
