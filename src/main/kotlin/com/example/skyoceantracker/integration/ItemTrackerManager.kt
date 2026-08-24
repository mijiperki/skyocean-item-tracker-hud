package com.example.skyoceantracker.integration

import net.minecraft.client.Minecraft
import net.minecraft.world.inventory.AbstractContainerMenu
import org.slf4j.LoggerFactory
import com.example.skyoceantracker.hud.ItemTrackerOverlay

object ItemTrackerManager {
    private val logger = LoggerFactory.getLogger("ItemTrackerManager")

    fun onItemTrackerKeybind() {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return
        val screen = minecraft.screen ?: return

        // Get the hovered slot from the current screen
        val hoveredSlot = try {
            minecraft.screen?.let { currentScreen ->
                if (currentScreen is net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<*>) {
                    val containerScreen = currentScreen as net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<*>
                    // Try to get hovered slot using reflection if needed
                    getHoveredSlot(containerScreen, minecraft)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Error getting hovered slot: ", e)
            null
        } ?
        if (hoveredSlot == null) {
            logger.info("No item hovered")
            return
        }

        val itemStack = hoveredSlot.item
        if (itemStack.isEmpty) {
            logger.info("Hovered item is empty")
            return
        }

        logger.info("Opening item tracker for: ${itemStack.hoverName.string}")
        ItemTrackerOverlay.setTrackedItem(itemStack)
    }

    private fun getHoveredSlot(
        containerScreen: net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<*>,
        minecraft: Minecraft
    ): net.minecraft.world.inventory.Slot? {
        val mouseX = minecraft.mouseHandler.xpos()
        val mouseY = minecraft.mouseHandler.ypos()

        // Container screen layout
        val containerX = containerScreen.leftPos
        val containerY = containerScreen.topPos

        // Check each slot in the container
        for (slot in containerScreen.menu.slots) {
            val slotX = containerX + slot.x
            val slotY = containerY + slot.y

            if (mouseX >= slotX && mouseX < slotX + 16 &&
                mouseY >= slotY && mouseY < slotY + 16) {
                return slot
            }
        }

        return null
    }
}
