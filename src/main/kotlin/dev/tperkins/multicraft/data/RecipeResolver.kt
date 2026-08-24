package dev.tperkins.multicraft.data

/**
 * Expands an item + desired amount into a [RecipeNode] tree, mirroring SkyOcean's
 * single-item Craft Helper. Pure JVM: no game state, fully unit-testable.
 *
 *  - Recurses through crafting recipes, multiplying ingredient quantities by the
 *    number of crafts needed at each level.
 *  - Memoises by (name, amount) so shared sub-items aren't recomputed.
 *  - Guards against recipe cycles using the current expansion path.
 *  - Caps depth so pathological data can't blow the stack.
 */
class RecipeResolver(
    private val repo: ItemRepository,
    private val maxDepth: Int = 32,
) {
    private data class Key(val name: String, val amount: Int)

    fun resolve(internalName: String, amount: Int): RecipeNode {
        require(amount > 0) { "amount must be positive, was $amount" }
        return expand(internalName.uppercase(), amount, HashSet(), HashMap(), depth = 0)
    }

    private fun expand(
        name: String,
        amount: Int,
        path: MutableSet<String>,
        memo: MutableMap<Key, RecipeNode>,
        depth: Int,
    ): RecipeNode {
        memo[Key(name, amount)]?.let { return it }

        val item = repo.item(name)
        val display = item?.displayName ?: name
        val recipe = item?.primaryCraftingRecipe()

        // Leaf conditions: no recipe, cycle on the current path, or depth cap.
        if (recipe == null || name in path || depth >= maxDepth) {
            return leaf(name, display, amount)
        }

        path.add(name)
        val output = recipe.outputCount.coerceAtLeast(1)
        val crafts = ceilDiv(amount, output)
        val children = recipe.ingredients.map { (ingName, perCraft) ->
            expand(ingName, perCraft * crafts, path, memo, depth + 1)
        }
        path.remove(name)

        val node = RecipeNode(
            internalName = name,
            displayName = display,
            requiredQty = amount,
            outputCountPerCraft = output,
            craftsNeeded = crafts,
            children = children,
        )
        memo[Key(name, amount)] = node
        return node
    }

    private fun leaf(name: String, display: String, amount: Int) = RecipeNode(
        internalName = name,
        displayName = display,
        requiredQty = amount,
        outputCountPerCraft = 1,
        craftsNeeded = amount,
        children = emptyList(),
    )

    private fun ceilDiv(a: Int, b: Int): Int = (a + b - 1) / b
}
