package dev.tperkins.multicraft

import dev.tperkins.multicraft.command.CraftCommands
import dev.tperkins.multicraft.config.Config
import dev.tperkins.multicraft.craft.CraftTargetManager
import dev.tperkins.multicraft.data.ItemRepository
import dev.tperkins.multicraft.data.RecipeResolver
import dev.tperkins.multicraft.inventory.ContainerScanner
import dev.tperkins.multicraft.ui.CraftHud
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

/**
 * Fabric client entrypoint. Loads config + recipe data, wires the runtime, and
 * registers the command, HUD and inventory-scanner hooks.
 */
class MultiCraftClient : ClientModInitializer {
    private val log = LoggerFactory.getLogger("MultiCraft")

    override fun onInitializeClient() {
        val configDir = FabricLoader.getInstance().configDir.resolve(MultiCraft.MOD_ID)

        val config = Config() // TODO: load/save config JSON (config/multicraft.json)
        val repo = loadRepository()
        val resolver = RecipeResolver(repo)
        val targets = CraftTargetManager(resolver, configDir.resolve("targets.json"))
        targets.load()

        MultiCraft.init(config, repo, resolver, targets)
        log.info("MultiCraft Helper loaded: {} items, {} active targets", repo.size, targets.size())

        CraftCommands.register()
        CraftHud.register()
        ContainerScanner.register()
    }

    /**
     * Bundled sample ships in the jar so the mod works offline out of the box.
     * A real deployment should also pull a full NEU/meowdding-repo snapshot into
     * the cache dir and merge it (see DESIGN.md §6). That download is intentionally
     * not wired here yet — it's the next data milestone.
     */
    private fun loadRepository(): ItemRepository {
        val bundled = javaClass.getResourceAsStream("/data/sample_recipes.json")
            ?.reader(Charsets.UTF_8)
            ?.use { ItemRepository.fromReader(it) }
            ?: ItemRepository.fromJson("{}")

        // TODO(data): if a cached full snapshot exists under config/multicraft/repo,
        //   load it and ItemRepository.merge(bundled, cached) with cached winning.
        return bundled
    }
}
