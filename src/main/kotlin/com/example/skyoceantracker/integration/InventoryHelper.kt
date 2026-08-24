package com.example.skyoceantracker.integration

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory

/**
 * Utilities for interacting with Minecraft's inventory system
 */
object InventoryHelper {
    private val logger = LoggerFactory.getLogger("InventoryHelper")

    /**
     * Get the slot currently being hovered over in a container screen
     */
    fun getHoveredSlot(containerScreen: AbstractContainerScreen<*>, minecraft: Minecraft): Slot? {
        val mouseX = minecraft.mouseHandler.xpos()
        val mouseY = minecraft.mouseHandler.ypos()

        val containerX = containerScreen.leftPos
        val containerY = containerScreen.topPos

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
