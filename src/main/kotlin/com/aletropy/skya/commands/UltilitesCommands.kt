package com.aletropy.skya.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object UtilitiesCommands
{
    private val gotoLocations = mapOf<String, Vector>(
        Pair("Lobby", Vector(10000.0, 100.0, 10000.0)),
    )

    val GOTO: LiteralCommandNode<CommandSourceStack> = Commands.literal("goto")
        .then(Commands.argument("name", StringArgumentType.string())
            .suggests { ctx, builder ->
                gotoLocations.forEach {
                    builder.suggest(it.key)
                }
                builder.buildFuture()
            }
            .requires { it.sender is Player }
            .executes { ctx ->
                val player = ctx.source.sender as Player
                val gotoName = ctx.getArgument("name", String::class.java) as String
                val position = gotoLocations[gotoName]

                if(position == null) {
                    player.sendMessage(Component.text("$gotoName don't exist in Goto locations", NamedTextColor.RED))
                    return@executes 1
                }

                val location = Location(Bukkit.getWorld("world")!!,
                    position.x, position.y, position.z)

                player.teleport(location)

                player.playSound(player.location, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f)
                player.sendMessage(Component.text("You teleported to $gotoName", NamedTextColor.AQUA))
                0
            }).build()
}