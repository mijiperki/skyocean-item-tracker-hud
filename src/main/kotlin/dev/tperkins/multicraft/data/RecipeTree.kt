package dev.tperkins.multicraft.data

/**
 * A resolved node in a craft tree: "to make [requiredQty] of [internalName] you
 * run its recipe [craftsNeeded] times, which needs these [children]".
 *
 * A node with no [children] is a leaf — a raw material we can't (or won't) expand
 * further (no known crafting recipe, cycle, or depth cap hit).
 */
data class RecipeNode(
    val internalName: String,
    val displayName: String,
    /** Total units of this item required by the parent context. */
    val requiredQty: Int,
    /** Units produced by one execution of this recipe (1 for leaves). */
    val outputCountPerCraft: Int,
    /** How many times the recipe must run: ceil(requiredQty / outputCountPerCraft). */
    val craftsNeeded: Int,
    val children: List<RecipeNode>,
) {
    val isLeaf: Boolean get() = children.isEmpty()

    /** Depth-first walk including this node. */
    fun walk(visit: (RecipeNode) -> Unit) {
        visit(this)
        children.forEach { it.walk(visit) }
    }

    /** Flattened raw-material totals (leaves), summed by internal name. */
    fun leafTotals(): Map<String, Int> {
        val totals = HashMap<String, Int>()
        walk { if (it.isLeaf) totals.merge(it.internalName, it.requiredQty, Int::plus) }
        return totals
    }
}
