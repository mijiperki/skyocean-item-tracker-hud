package com.example.skyoceantracker.hud.display

import com.example.skyoceantracker.hud.ItemTrackerOverlay
import com.example.skyoceantracker.hud.ItemTrackerTree
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import org.slf4j.LoggerFactory

/**
 * Handles rendering of the Item Tracker overlay on screen
 * Mimics SkyOcean's CraftHelperDisplay rendering style
 */
object ItemTrackerDisplay : HudRenderCallback {
    private val logger = LoggerFactory.getLogger("ItemTrackerDisplay")

    private var isVisible = false
    private var lastTree: ItemTrackerTree? = null

    init {
        HudRenderCallback.EVENT.register(this)
    }

    override fun onHudRender(guiGraphics: GuiGraphics, partialTick: Float) {
        if (!isVisible) return

        val tree = ItemTrackerOverlay.getCurrentTree() ?: return
        renderTree(guiGraphics, tree)
    }

    fun refresh() {
        val tree = ItemTrackerOverlay.getCurrentTree()
        if (tree != null) {
            isVisible = true
            lastTree = tree
            logger.info("ItemTrackerDisplay refreshed")
        }
    }

    fun clear() {
        isVisible = false
        lastTree = null
    }

    private fun renderTree(guiGraphics: GuiGraphics, tree: ItemTrackerTree) {
        // TODO: Implement rendering
        // This should:
        // 1. Draw the background (similar to SkyOcean's tooltip background)
        // 2. Draw the output item with name
        // 3. Draw +/- buttons for quantity
        // 4. Draw the tree structure with indentation
        // 5. Show progress for each item (available/required)

        val x = 10
        val y = 10

        // Placeholder: Just render item name for now
        guiGraphics.drawString(
            guiGraphics.guiRenderingContext.font,
            "${tree.item.hoverName.string}",
            x,
            y,
            0xFFFFFF
        )
    }
}
