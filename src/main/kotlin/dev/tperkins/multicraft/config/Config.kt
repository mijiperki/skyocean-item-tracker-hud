package dev.tperkins.multicraft.config

import dev.tperkins.multicraft.inventory.Source

/**
 * Runtime configuration. Kept as a plain data class so it serialises cleanly and
 * carries no Minecraft dependency. A proper settings screen (Resourceful Config /
 * YACL) can be layered on later — see DESIGN.md §11.
 */
data class Config(
    var hudEnabled: Boolean = true,
    var hudAnchor: Anchor = Anchor.TOP_LEFT,
    var hudX: Int = 4,
    var hudY: Int = 4,
    var hudScale: Float = 1.0f,
    /** Collapse newly added targets to just their header row. */
    var collapseNewTargets: Boolean = false,
    /** Sources counted toward "owned" quantities. */
    var countedSources: MutableSet<Source> = Source.entries.toMutableSet(),
    /** Warn in the HUD when a source's data is older than this (millis). */
    var staleThresholdMillis: Long = 5 * 60 * 1000,
) {
    enum class Anchor { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
}
