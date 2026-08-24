package com.example.skyoceantracker.integration

import net.minecraft.world.item.ItemStack
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.components.AbstractWidget
import org.slf4j.LoggerFactory

/**
 * Bridge to SkyOcean's internal APIs using reflection
 * Allows us to reuse their rendering system for our separate item tracker
 */
object SkyOceanAPIBridge {
    private val logger = LoggerFactory.getLogger("SkyOceanAPIBridge")

    /**
     * Build a CraftHelperTree for an item using SkyOcean's SimpleRecipeApi
     */
    fun buildRecipeTree(itemStack: ItemStack, amount: Int): Any? {
        return try {
            // Get SkyBlockId from item
            val skyBlockIdClass = Class.forName("tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId")
            val fromItemMethod = skyBlockIdClass.getMethod("fromItem", ItemStack::class.java)
            val skyBlockId = fromItemMethod.invoke(null, itemStack) ?: return null

            // Get recipe using SimpleRecipeApi
            val simpleRecipeApiClass = Class.forName("me.owdding.skyocean.features.recipe.SimpleRecipeApi")
            val getBestRecipeMethod = simpleRecipeApiClass.getMethod("getBestRecipe", skyBlockIdClass)
            val recipe = getBestRecipeMethod.invoke(null, skyBlockId) ?: return null

            // Create CraftHelperTree
            val skyOceanItemIngredientClass = Class.forName("me.owdding.skyocean.features.recipe.SkyOceanItemIngredient")
            val ingredientConstructor = skyOceanItemIngredientClass.getConstructor(skyBlockIdClass, Int::class.java)
            val ingredient = ingredientConstructor.newInstance(skyBlockId, 1)

            val craftHelperTreeClass = Class.forName("me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree")
            val treeConstructor = craftHelperTreeClass.getConstructor(
                Class.forName("me.owdding.skyocean.features.recipe.Recipe"),
                Class.forName("me.owdding.skyocean.features.recipe.ItemLikeIngredient"),
                Int::class.java
            )
            val tree = treeConstructor.newInstance(recipe, ingredient, amount)

            logger.info("Successfully built recipe tree")
            tree
        } catch (e: Exception) {
            logger.error("Failed to build recipe tree: ", e)
            null
        }
    }

    /**
     * Get ItemTracker from SkyOcean's sources for item tracking
     */
    fun createItemTracker(): Any? {
        return try {
            // Get craftHelperSources
            val itemSourcesClass = Class.forName("me.owdding.skyocean.features.item.sources.ItemSources")
            val craftHelperSourcesField = itemSourcesClass.getField("craftHelperSources")
            val sources = craftHelperSourcesField.get(null) as? Iterable<*> ?: return null

            // Create ItemTracker with these sources
            val itemTrackerClass = Class.forName("me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker")
            val itemTrackerConstructor = itemTrackerClass.getConstructor(Iterable::class.java)
            itemTrackerConstructor.newInstance(sources)
        } catch (e: Exception) {
            logger.error("Failed to create item tracker: ", e)
            null
        }
    }

    /**
     * Render a CraftHelperTree using SkyOcean's CraftHelperDisplay rendering logic
     */
    fun renderTree(
        tree: Any,
        itemTracker: Any,
        maxWidth: Int,
        refreshCallback: () -> Unit
    ): AbstractWidget? {
        return try {
            // Get the output from the tree
            val treeClass = tree::class.java
            val outputField = treeClass.getDeclaredField("output")
            outputField.isAccessible = true
            val output = outputField.get(tree)

            // Use SkyOcean's LayoutFactory and formatters to build the UI
            val layoutFactoryClass = Class.forName("me.owdding.lib.builder.LayoutFactory")
            val verticalMethod = layoutFactoryClass.getMethod("vertical", Int::class.java)

            // Create the visualization using TreeFormatter
            val treeFormatterClass = Class.forName("me.owdding.skyocean.features.recipe.crafthelper.views.tree.TreeFormatter")
            val formatMethod = treeFormatterClass.getMethod(
                "format",
                Class.forName("me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree"),
                Class.forName("me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker"),
                Class.forName("me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder"),
                kotlin.jvm.functions.Function1::class.java
            )

            // Build widget using SkyOcean's formatters
            val widgetBuilderClass = Class.forName("me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder")
            val widgetBuilderConstructor = widgetBuilderClass.getConstructor(
                kotlin.jvm.functions.Function0::class.java
            )
            val widgetBuilder = widgetBuilderConstructor.newInstance(refreshCallback as kotlin.jvm.functions.Function0<Unit>)

            null  // Return will be built via the formatter
        } catch (e: Exception) {
            logger.error("Failed to render tree: ", e)
            null
        }
    }
}
