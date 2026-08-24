package dev.tperkins.multicraft

import dev.tperkins.multicraft.config.Config
import dev.tperkins.multicraft.craft.CraftTargetManager
import dev.tperkins.multicraft.data.ItemRepository
import dev.tperkins.multicraft.data.RecipeResolver
import dev.tperkins.multicraft.inventory.QuantityIndex

/**
 * Process-wide runtime state, initialised once by [MultiCraftClient].
 *
 * Split out from the entrypoint so the command/HUD/scanner layers can reach the
 * repo, resolver, target manager and quantity index without threading references
 * everywhere.
 */
object MultiCraft {
    const val MOD_ID = "multicraft"

    lateinit var config: Config
        private set
    lateinit var repo: ItemRepository
        private set
    lateinit var resolver: RecipeResolver
        private set
    lateinit var targets: CraftTargetManager
        private set
    val quantities: QuantityIndex = QuantityIndex()

    fun init(config: Config, repo: ItemRepository, resolver: RecipeResolver, targets: CraftTargetManager) {
        this.config = config
        this.repo = repo
        this.resolver = resolver
        this.targets = targets
    }
}
