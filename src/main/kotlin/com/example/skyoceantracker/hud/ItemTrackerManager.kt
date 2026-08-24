package com.example.skyoceantracker.hud

import net.minecraft.world.item.ItemStack
import org.slf4j.LoggerFactory
import com.example.skyoceantracker.integration.SkyOceanAPIBridge

/**
 * Manages a separate item tracker tree system that mimics CraftHelper
 * but stores its own state independent of SkyOcean's CraftHelperStorage
 */
object ItemTrackerManager {
    private val logger = LoggerFactory.getLogger("ItemTrackerManager")

    private var currentItem: ItemStack? = null
    private var currentAmount: Int = 1
    private var currentTree: Any? = null  // Will be CraftHelperTree from SkyOcean

    /**
     * Set an item to track independently from SkyOcean's craft helper
     */
    fun setTrackedItem(itemStack: ItemStack, amount: Int = 1) {
        logger.info("Setting tracked item: ${itemStack.hoverName.string} x$amount")
        currentItem = itemStack
        currentAmount = amount
        
        // Build the tree using SkyOcean's recipe system
        currentTree = SkyOceanAPIBridge.buildRecipeTree(itemStack, amount)
        
        if (currentTree == null) {
            logger.warn("Could not build recipe tree for item")
            return
        }
        
        // Show the overlay
        ItemTrackerDisplayBridge.showOverlay(itemStack, amount, currentTree!!)
    }

    fun setAmount(newAmount: Int) {
        if (currentItem == null) return
        currentAmount = newAmount
        currentTree = SkyOceanAPIBridge.buildRecipeTree(currentItem!!, newAmount)
        if (currentTree != null) {
            ItemTrackerDisplayBridge.showOverlay(currentItem!!, newAmount, currentTree!!)
        }
    }

    fun clear() {
        currentItem = null
        currentAmount = 1
        currentTree = null
        ItemTrackerDisplayBridge.hideOverlay()
    }

    fun getCurrentTree(): Any? = currentTree
    fun getCurrentItem(): ItemStack? = currentItem
    fun getCurrentAmount(): Int = currentAmount
}
