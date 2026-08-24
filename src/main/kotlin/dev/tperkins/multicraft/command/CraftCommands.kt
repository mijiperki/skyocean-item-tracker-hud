package dev.tperkins.multicraft.command

/*
 * ---------------------------------------------------------------------------
 * MINECRAFT INTEGRATION LAYER
 * Targets Minecraft 26.1.2 (deobfuscated) + Fabric client-command API v2.
 * On 26.1 the static command builders live on ClientCommands (not the old
 * ClientCommandManager). The logic they call into (MultiCraft.targets, resolver,
 * repo) is verified pure-JVM code.
 * ---------------------------------------------------------------------------
 */

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import dev.tperkins.multicraft.MultiCraft
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

/**
 * `/mch ...` client commands. This is the multi-target control surface: unlike
 * SkyOcean's single active recipe, `add` accumulates targets rather than
 * replacing the current one.
 */
object CraftCommands {

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("mch")
                    .then(
                        literal("add").then(
                            argument("item", StringArgumentType.string())
                                .executes { ctx -> add(ctx.source, item(ctx), 1) }
                                .then(
                                    argument("amount", IntegerArgumentType.integer(1))
                                        .executes { ctx -> add(ctx.source, item(ctx), amount(ctx)) }
                                )
                        )
                    )
                    .then(
                        literal("remove").then(
                            argument("item", StringArgumentType.string())
                                .executes { ctx -> remove(ctx.source, item(ctx)) }
                        )
                    )
                    .then(literal("clear").executes { ctx -> clear(ctx.source) })
                    .then(literal("list").executes { ctx -> list(ctx.source) })
                    .then(
                        literal("move").then(
                            argument("item", StringArgumentType.string())
                                .then(literal("up").executes { ctx -> move(ctx.source, item(ctx), -1) })
                                .then(literal("down").executes { ctx -> move(ctx.source, item(ctx), 1) })
                        )
                    )
                    .then(
                        literal("collapse").then(
                            argument("item", StringArgumentType.string())
                                .executes { ctx -> collapse(ctx.source, item(ctx)) }
                        )
                    )
                    .then(literal("hud").executes { ctx -> toggleHud(ctx.source) })
            )
        }
    }

    private fun item(ctx: com.mojang.brigadier.context.CommandContext<FabricClientCommandSource>) =
        StringArgumentType.getString(ctx, "item").uppercase()

    private fun amount(ctx: com.mojang.brigadier.context.CommandContext<FabricClientCommandSource>) =
        IntegerArgumentType.getInteger(ctx, "amount")

    private fun add(source: FabricClientCommandSource, item: String, amount: Int): Int {
        if (MultiCraft.repo.item(item) == null) {
            source.reply("§cUnknown item: $item")
            return 0
        }
        val target = MultiCraft.targets.add(item, amount)
        if (MultiCraft.config.collapseNewTargets) target.collapsed = true
        source.reply("§aTracking §f${target.displayName} §7x$amount §7(${MultiCraft.targets.size()} active)")
        return 1
    }

    private fun remove(source: FabricClientCommandSource, item: String): Int {
        val ok = MultiCraft.targets.remove(item)
        source.reply(if (ok) "§aStopped tracking §f$item" else "§7$item was not tracked")
        return if (ok) 1 else 0
    }

    private fun clear(source: FabricClientCommandSource): Int {
        val n = MultiCraft.targets.size()
        MultiCraft.targets.clear()
        source.reply("§aCleared $n craft target(s)")
        return 1
    }

    private fun list(source: FabricClientCommandSource): Int {
        val all = MultiCraft.targets.all()
        if (all.isEmpty()) {
            source.reply("§7No active craft targets. Use §f/mch add <item> [amount]")
            return 1
        }
        source.reply("§6Active craft targets (${all.size}):")
        all.forEachIndexed { i, t -> source.reply("§7${i + 1}. §f${t.displayName} §7x${t.desiredAmount}") }
        return 1
    }

    private fun move(source: FabricClientCommandSource, item: String, delta: Int): Int {
        val ok = MultiCraft.targets.move(item, delta)
        if (ok) source.reply("§aMoved §f$item")
        return if (ok) 1 else 0
    }

    private fun collapse(source: FabricClientCommandSource, item: String): Int {
        MultiCraft.targets.toggleCollapsed(item)
        return 1
    }

    private fun toggleHud(source: FabricClientCommandSource): Int {
        MultiCraft.config.hudEnabled = !MultiCraft.config.hudEnabled
        source.reply(if (MultiCraft.config.hudEnabled) "§aHUD shown" else "§7HUD hidden")
        return 1
    }

    private fun FabricClientCommandSource.reply(msg: String) =
        sendFeedback(Component.literal(msg))
}
