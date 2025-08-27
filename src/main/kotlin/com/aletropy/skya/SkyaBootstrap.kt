package com.aletropy.skya

import com.aletropy.skya.commands.CommandsManager
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player

class SkyaBootstrap : PluginBootstrap
{
    override fun bootstrap(ctx: BootstrapContext) {
        ctx.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, {commands ->
            CommandsManager.register(commands)
        })
    }
}