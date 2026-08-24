package dev.tperkins.multicraft.inventory

/*
 * ---------------------------------------------------------------------------
 * MINECRAFT INTEGRATION LAYER  (scaffold — see DESIGN.md §9)
 * The event wiring and item-stack reads below target the 1.21.x Fabric/Mojmap
 * shape and MUST be verified against 26.1.2 mappings. In particular, NBT access
 * moved to DataComponents (1.20.5+): SkyBlock's legacy `ExtraAttributes.id` now
 * arrives inside the `minecraft:custom_data` component. The SkyBlock-id
 * extraction contract is captured in [skyblockId]; its body is the part most
 * likely to need mapping fixes.
 * ---------------------------------------------------------------------------
 */

import dev.tperkins.multicraft.MultiCraft

/**
 * Watches open container/inventory screens and feeds observed item counts into
 * [MultiCraft.quantities]. One [Source] is refreshed per observed screen so
 * moving stacks between screens doesn't double-count.
 *
 * This is the "item finding" half of the mod. v1 targets the player inventory and
 * ender chest (always readable); storage pages and the sacks menu ([SackTracker])
 * follow once screen-title detection is in.
 */
object ContainerScanner {

    fun register() {
        // TODO(mc): register a client tick / ScreenEvents.AFTER_INIT hook.
        //   On each relevant screen:
        //     val observed = scanSlots(screen) ; classify source from title ;
        //     MultiCraft.quantities.updateSource(source, observed, now)
        //   The player inventory can be scanned every tick regardless of screen.
    }

    /**
     * Collapse a set of stacks into internalName -> total count.
     * @param stacks pairs of (skyblockInternalName?, stackCount). Nulls (vanilla /
     *   unidentifiable items) are skipped.
     */
    fun tally(stacks: Sequence<Pair<String?, Int>>): Map<String, Int> {
        val out = HashMap<String, Int>()
        for ((id, count) in stacks) {
            if (id == null || count <= 0) continue
            out.merge(id.uppercase(), count, Int::plus)
        }
        return out
    }

    /**
     * Extract the SkyBlock internal id from an item stack's custom data.
     *
     * Contract (implementation pending mapping verification):
     *   - Read the `minecraft:custom_data` component.
     *   - Return the `ExtraAttributes.id` string (e.g. "ENCHANTED_IRON").
     *   - Return null for vanilla items or anything without that tag.
     *
     * Kept as a single function so the mapping-sensitive NBT access is isolated.
     */
    fun skyblockId(customDataIdTag: String?): String? =
        customDataIdTag?.takeIf { it.isNotBlank() }?.uppercase()
}
