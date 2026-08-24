package com.example.skyoceantracker.integration

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import org.slf4j.LoggerFactory
import com.example.skyoceantracker.hud.ItemTrackerManager

/**
 * Handles screen events and keybind input for the item tracker
 */
object ScreenEventListener {
    private val logger = LoggerFactory.getLogger("ScreenEventListener")
    private var screenRefreshRequested = false

    fun register() {
        // Listen for screen initialization to add our overlay
        ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
            if (screen is AbstractContainerScreen<*>) {
                onScreenInit(screen)
            }
        }

        // Listen for mouse clicks
        ScreenEvents.BEFORE_MOUSE_CLICK.register { client, screen, mouseX, mouseY, button ->
            false // Don't consume event
        }
    }

    private fun onScreenInit(screen: AbstractContainerScreen<*>) {
        if (screenRefreshRequested) {
            screenRefreshRequested = false
            logger.info("Screen refreshed, updating item tracker overlay")
        }
    }

    fun notifyScreenRefresh() {
        screenRefreshRequested = true
    }
}
