package com.example.skyoceantracker.keybind

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import com.example.skyoceantracker.integration.InventoryHelper
import com.example.skyoceantracker.hud.ItemTrackerManager
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import org.slf4j.LoggerFactory

object ItemTrackerKeybinds {
    private val logger = LoggerFactory.getLogger("ItemTrackerKeybinds")

    private val itemTrackerKey = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.skyoceantracker.item_tracker",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SLASH,
            "category.skyoceantracker.item_tracker"
        )
    )

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            while (itemTrackerKey.consumeClick()) {
                onItemTrackerKeybind(client)
            }
        })
    }

    private fun onItemTrackerKeybind(client: Minecraft) {
        val screen = client.screen
        if (screen !is AbstractContainerScreen<*>) {
            logger.debug("Not in a container screen")
            return
        }

        val hoveredSlot = InventoryHelper.getHoveredSlot(screen, client)
        if (hoveredSlot == null) {
            logger.debug("No hovered slot")
            return
        }

        val itemStack = hoveredSlot.item
        if (itemStack.isEmpty) {
            logger.debug("Hovered item is empty")
            return
        }

        logger.info("Item tracker keybind pressed for: ${itemStack.hoverName.string}")
        ItemTrackerManager.setTrackedItem(itemStack, 1)
    }
}
