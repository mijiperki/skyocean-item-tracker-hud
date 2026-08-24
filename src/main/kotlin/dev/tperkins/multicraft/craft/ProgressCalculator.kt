package dev.tperkins.multicraft.craft

import dev.tperkins.multicraft.data.RecipeNode
import dev.tperkins.multicraft.inventory.OwnedProvider

/**
 * Annotated view of a [RecipeNode] with the player's current progress toward it.
 *
 * A node is [satisfied] when you either already own enough of the item itself, or
 * you own enough of every child to craft it. [percent] is a 0..1 completion ratio
 * weighted by leaf-material quantity, matching how the single-item helper fills in.
 */
data class ProgressNode(
    val recipe: RecipeNode,
    val owned: Int,
    val required: Int,
    val satisfied: Boolean,
    val percent: Double,
    val children: List<ProgressNode>,
) {
    val remaining: Int get() = (required - owned).coerceAtLeast(0)
}

/**
 * Walks a resolved recipe tree and layers the player's owned quantities on top.
 *
 * IMPORTANT (matches the spec): each craft target is scored independently. If two
 * targets both need Enchanted Iron, each sees your full owned count — exactly like
 * running the single-item helper twice. There is intentionally no cross-target
 * budgeting here.
 */
object ProgressCalculator {

    fun evaluate(root: RecipeNode, owned: OwnedProvider): ProgressNode = visit(root, owned)

    private fun visit(node: RecipeNode, owned: OwnedProvider): ProgressNode {
        val have = owned.owned(node.internalName)
        val children = node.children.map { visit(it, owned) }

        val satisfied = have >= node.requiredQty ||
            (children.isNotEmpty() && children.all { it.satisfied })

        val percent = when {
            have >= node.requiredQty -> 1.0
            node.isLeaf -> (have.toDouble() / node.requiredQty).coerceIn(0.0, 1.0)
            else -> weightedChildPercent(node, children, have)
        }

        return ProgressNode(
            recipe = node,
            owned = have,
            required = node.requiredQty,
            satisfied = satisfied,
            percent = percent,
            children = children,
        )
    }

    /**
     * For an intermediate node, blend "how much of it I already hold" with the
     * quantity-weighted progress of its children, so partially-gathered subtrees
     * read as partially complete.
     */
    private fun weightedChildPercent(
        node: RecipeNode,
        children: List<ProgressNode>,
        have: Int,
    ): Double {
        val directShare = (have.toDouble() / node.requiredQty).coerceIn(0.0, 1.0)
        val leafWeight = children.sumOf { it.recipe.leafTotals().values.sum() }
        if (leafWeight == 0) return directShare
        val childProgress = children.sumOf { child ->
            val w = child.recipe.leafTotals().values.sum()
            child.percent * w
        } / leafWeight
        // Own-stock counts as already done; take the better of the two views.
        return maxOf(directShare, childProgress).coerceIn(0.0, 1.0)
    }
}
