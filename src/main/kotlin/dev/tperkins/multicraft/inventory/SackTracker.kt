package dev.tperkins.multicraft.inventory

/*
 * MINECRAFT INTEGRATION LAYER (scaffold — see DESIGN.md §9).
 * Sack contents aren't continuously available: they're only known when the sacks
 * GUI is open, or from chat/scoreboard deltas. We cache last-seen counts with a
 * timestamp and surface staleness via QuantityIndex.ageMillis.
 */

/**
 * Parses the Sacks menu into internalName -> count.
 *
 * The sacks menu shows each material as an item whose lore contains
 * "Stored: <n>/<cap>". This object only holds the pure parsing of that lore line;
 * the screen hook that supplies the strings lives in [ContainerScanner]'s wiring.
 */
object SackTracker {

    private val STORED = Regex("""Stored:\s*([\d,]+)""", RegexOption.IGNORE_CASE)

    /**
     * @param items pairs of (skyblockInternalName, loreLines) as read from the
     *   sacks GUI. Returns the parsed stored count per material.
     */
    fun parse(items: Sequence<Pair<String, List<String>>>): Map<String, Int> {
        val out = HashMap<String, Int>()
        for ((id, lore) in items) {
            val count = lore.firstNotNullOfOrNull { line ->
                STORED.find(stripFormatting(line))?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull()
            } ?: continue
            out.merge(id.uppercase(), count, Int::plus)
        }
        return out
    }

    private fun stripFormatting(s: String) = s.replace(Regex("§."), "")
}
