package com.example.skyoceantracker

import com.example.skyoceantracker.keybind.ItemTrackerKeybinds
import com.example.skyoceantracker.integration.ScreenEventListener
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import org.slf4j.LoggerFactory

@Environment(EnvType.CLIENT)
object SkyOceanTrackerMod : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("SkyOceanTracker")

    override fun onInitializeClient() {
        logger.info("SkyOcean Item Tracker HUD initialized!")
        ItemTrackerKeybinds.register()
        ScreenEventListener.register()
        logger.info("Keybinds and screen listeners registered")
    }
}
