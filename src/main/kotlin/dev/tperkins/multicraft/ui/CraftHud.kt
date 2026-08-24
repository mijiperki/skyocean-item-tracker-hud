package dev.tperkins.multicraft.ui

/*
 * ---------------------------------------------------------------------------
 * MINECRAFT INTEGRATION LAYER
 * HUD rendering changed across recent versions (HudRenderCallback ->
 * HudLayerRegistrationCallback / HudElementRegistry). The registration and
 * GuiGraphics draw calls below target the 1.21.x shape and MUST be verified
 * against 26.1.2 mappings. The per-target line model comes from [TargetWidget],
 * which is pure, verified code — only the drawing here is mapping-sensitive.
 * ---------------------------------------------------------------------------
 */

import dev.tperkins.multicraft.MultiCraft
import dev.tperkins.multicraft.config.Config
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

/**
 * Draws every active craft target, stacked, in one anchored panel. This is the
 * multi-item display: [TargetWidget] renders each target's tree independently and
 * we lay them out one after another.
 */
object CraftHud {
    private const val LINE_HEIGHT = 10
    private const val INDENT_PX = 8
    private const val PANEL_GAP = 4

    fun register() {
        HudRenderCallback.EVENT.register(HudRenderCallback { graphics, _ ->
            render(graphics)
        })
    }

    private fun render(graphics: GuiGraphics) {
        val config = MultiCraft.config
        if (!config.hudEnabled || MultiCraft.targets.isEmpty()) return
        val mc = Minecraft.getInstance()
        if (mc.options.hideGui) return
        val font = mc.font

        // Build the full line set first so we can anchor the whole panel.
        val panels = MultiCraft.targets.all().map { TargetWidget.build(it, MultiCraft.quantities) }
        if (panels.all { it.isEmpty() }) return

        val panelHeight = panels.sumOf { it.size * LINE_HEIGHT } + (panels.size - 1) * PANEL_GAP
        val panelWidth = panels.flatten().maxOf { line ->
            font.width(line.text) + line.indent * INDENT_PX
        }

        val screenW = graphics.guiWidth()
        val screenH = graphics.guiHeight()
        var x = anchorX(config, screenW, panelWidth)
        var y = anchorY(config, screenH, panelHeight)

        for (panel in panels) {
            for (line in panel) {
                graphics.drawString(font, line.text, x + line.indent * INDENT_PX, y, line.argb, true)
                y += LINE_HEIGHT
            }
            y += PANEL_GAP
        }
    }

    private fun anchorX(config: Config, screenW: Int, panelW: Int): Int = when (config.hudAnchor) {
        Config.Anchor.TOP_LEFT, Config.Anchor.BOTTOM_LEFT -> config.hudX
        Config.Anchor.TOP_RIGHT, Config.Anchor.BOTTOM_RIGHT -> screenW - panelW - config.hudX
    }

    private fun anchorY(config: Config, screenH: Int, panelH: Int): Int = when (config.hudAnchor) {
        Config.Anchor.TOP_LEFT, Config.Anchor.TOP_RIGHT -> config.hudY
        Config.Anchor.BOTTOM_LEFT, Config.Anchor.BOTTOM_RIGHT -> screenH - panelH - config.hudY
    }
}
