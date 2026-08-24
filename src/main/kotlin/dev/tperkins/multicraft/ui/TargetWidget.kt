package dev.tperkins.multicraft.ui

import dev.tperkins.multicraft.craft.CraftTarget
import dev.tperkins.multicraft.craft.ProgressCalculator
import dev.tperkins.multicraft.craft.ProgressNode
import dev.tperkins.multicraft.inventory.OwnedProvider

/** One rendered line of a target widget: indented, coloured, plain text. */
data class HudLine(
    val text: String,
    val argb: Int,
    val indent: Int,
    val satisfied: Boolean,
)

/**
 * Builds the display model for a single craft target — the equivalent of one
 * SkyOcean Craft Helper GUI element. Pure JVM (produces [HudLine]s); [CraftHud]
 * turns them into draw calls. Multiple of these render at once, which is the
 * whole point of the mod.
 */
object TargetWidget {

    private const val GREEN = 0xFF55FF55.toInt()
    private const val GRAY = 0xFFAAAAAA.toInt()
    private const val GOLD = 0xFFFFAA00.toInt()

    /** Build all lines for [target]; collapsed targets show only the header. */
    fun build(target: CraftTarget, owned: OwnedProvider): List<HudLine> {
        val progress = ProgressCalculator.evaluate(target.tree, owned)
        val lines = ArrayList<HudLine>()
        lines += header(target, progress)
        if (!target.collapsed) appendChildren(progress, depth = 1, out = lines)
        return lines
    }

    private fun header(target: CraftTarget, progress: ProgressNode): HudLine {
        val pct = (progress.percent * 100).toInt()
        val marker = if (progress.satisfied) "§a✔" else "§6▶"
        return HudLine(
            text = "$marker §f${strip(target.displayName)} §7x${target.desiredAmount} §8[$pct%]",
            argb = if (progress.satisfied) GREEN else GOLD,
            indent = 0,
            satisfied = progress.satisfied,
        )
    }

    private fun appendChildren(node: ProgressNode, depth: Int, out: MutableList<HudLine>) {
        for (child in node.children) {
            out += line(child, depth)
            if (child.children.isNotEmpty() && !child.satisfied) {
                appendChildren(child, depth + 1, out)
            }
        }
    }

    private fun line(node: ProgressNode, depth: Int): HudLine {
        val name = strip(node.recipe.displayName)
        val text = if (node.satisfied) {
            "§a✔ $name §7${node.owned}/${node.required}"
        } else {
            "§7• §f$name §e${node.owned}§7/§f${node.required}"
        }
        return HudLine(text, if (node.satisfied) GREEN else GRAY, depth, node.satisfied)
    }

    private fun strip(s: String) = s.replace(Regex("§."), "")
}
