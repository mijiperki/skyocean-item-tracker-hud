package com.example.skyoceantracker

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import org.slf4j.LoggerFactory

@Environment(EnvType.CLIENT)
object SkyOceanTrackerMod : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("SkyOceanTracker")

    override fun onInitializeClient() {
        logger.info("SkyOcean Item Tracker HUD initialized!")
        // Initialize your HUD overlay here
    }
}
