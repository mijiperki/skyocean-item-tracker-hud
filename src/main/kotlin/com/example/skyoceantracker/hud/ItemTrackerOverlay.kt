package com.example.skyoceantracker.hud

import net.minecraft.world.item.ItemStack
import org.slf4j.LoggerFactory
import com.example.skyoceantracker.hud.display.ItemTrackerDisplay

/**
 * Main controller for the item tracker overlay
 * Manages the lifecycle of item tracker trees and display
 */
object ItemTrackerOverlay {
    private val logger = LoggerFactory.getLogger("ItemTrackerOverlay")

    private var currentTrackedItem: ItemStack? = null
    private var currentTrackerTree: ItemTrackerTree? = null

    fun setTrackedItem(itemStack: ItemStack) {
        logger.info("Setting tracked item: ${itemStack.hoverName.string}")
        this.currentTrackedItem = itemStack
        this.currentTrackerTree = ItemTrackerTree.create(itemStack)
        ItemTrackerDisplay.refresh()
    }

    fun getCurrentTree(): ItemTrackerTree? = currentTrackerTree
    fun getCurrentItem(): ItemStack? = currentTrackedItem

    fun clear() {
        currentTrackedItem = null
        currentTrackerTree = null
        ItemTrackerDisplay.clear()
    }
}
