package dev.tperkins.multicraft.data

/**
 * A single recipe producing some item.
 *
 * In the NEU schema a crafting recipe is a 3x3 grid keyed A1..C3 whose values are
 * "INTERNALNAME:qty" (empty string for blank slots), plus a "count" for how many
 * of the output a single craft yields. We flatten the grid into a summed
 * ingredient map because the craft helper only cares about totals, not layout.
 */
data class Recipe(
    /** e.g. "crafting", "forge", "npc_shop". Only "crafting" is expanded for now. */
    val type: String,
    /** How many of the output item one execution of this recipe yields. */
    val outputCount: Int,
    /** ingredient internalName -> total quantity required for one execution. */
    val ingredients: Map<String, Int>,
)
