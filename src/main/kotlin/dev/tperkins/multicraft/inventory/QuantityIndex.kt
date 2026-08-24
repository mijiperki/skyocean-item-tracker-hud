package dev.tperkins.multicraft.inventory

import java.util.concurrent.ConcurrentHashMap

/** Where an owned quantity was observed. Used for staleness display in the HUD. */
enum class Source { INVENTORY, ENDER_CHEST, STORAGE, SACKS, ACCESSORY_BAG, VAULT }

/** Anything that can answer "how many <internalName> do I own?" */
fun interface OwnedProvider {
    fun owned(internalName: String): Int
}

/**
 * Aggregate of how many of each SkyBlock item the player currently has, broken
 * down by [Source]. Populated by the Minecraft-facing scanners
 * (ContainerScanner, SackTracker); consumed by [dev.tperkins.multicraft.craft.ProgressCalculator].
 *
 * Pure JVM and thread-safe: scanners run on the client thread, the HUD reads on
 * the render thread.
 */
class QuantityIndex : OwnedProvider {
    // internalName -> (source -> count). Latest snapshot per source wins.
    private val counts = ConcurrentHashMap<String, MutableMap<Source, Int>>()
    private val lastSeen = ConcurrentHashMap<Source, Long>()

    /**
     * Replace the counts contributed by a single [source] wholesale. Scanners
     * observe one container/screen at a time, so per-source replacement avoids
     * double-counting stacks that moved between screens.
     */
    fun updateSource(source: Source, observed: Map<String, Int>, nowMillis: Long) {
        // Drop this source's old contribution first.
        counts.values.forEach { it.remove(source) }
        for ((name, qty) in observed) {
            if (qty <= 0) continue
            counts.getOrPut(name.uppercase()) { ConcurrentHashMap() }[source] = qty
        }
        lastSeen[source] = nowMillis
    }

    override fun owned(internalName: String): Int =
        counts[internalName.uppercase()]?.values?.sum() ?: 0

    fun ownedBySource(internalName: String): Map<Source, Int> =
        counts[internalName.uppercase()]?.toMap() ?: emptyMap()

    /** Millis since a source was last refreshed, or null if never seen. */
    fun ageMillis(source: Source, nowMillis: Long): Long? =
        lastSeen[source]?.let { nowMillis - it }

    fun clear() {
        counts.clear()
        lastSeen.clear()
    }
}
