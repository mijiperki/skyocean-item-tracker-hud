package com.example.skyoceantracker.keybind

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import com.example.skyoceantracker.integration.ItemTrackerManager

object ItemTrackerKeybinds {
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
                ItemTrackerManager.onItemTrackerKeybind()
            }
        })
    }
}
