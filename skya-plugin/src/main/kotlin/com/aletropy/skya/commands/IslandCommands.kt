package com.aletropy.skya.commands

import com.aletropy.skya.Skya
import com.aletropy.skya.island.Islands
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player

object IslandCommands
{
    var dbManager = Skya.INSTANCE.dbManager
    var islandManager = Skya.INSTANCE.islandManager

    val ISLAND: LiteralCommandNode<CommandSourceStack> = Commands.literal("island")
        .then(Commands.literal("admin")
            .requires { it.sender is Player && (it.sender as Player).isOp }
            .then(Commands.literal("deleteAllFromGroup")
                .then(Commands.argument("groupId", StringArgumentType.greedyString())
                    .suggests { ctx, builder ->
                        dbManager.getAllGroups().forEach { builder.suggest(it.name) }
                        builder.buildFuture()
                    }
                    .executes { ctx ->
                        val player = ctx.source.sender as Player
                        val groupName = ctx.getArgument("groupId", String::class.java)
                        val groupId = dbManager.getGroupByName(groupName)?.id ?: return@executes 1

                        val deletedIslands = islandManager.deleteAllIslandsFromGroup(groupId)

                        if(deletedIslands == 0)
                        {
                            player.sendMessage(Component.text("This group has no islands.", NamedTextColor.YELLOW))
                            return@executes 1
                        }

                        player.sendMessage(Component.text("Deleted $deletedIslands islands from this group.", NamedTextColor.GREEN))
                      0
                    })
            )
        )
        .then(Commands.literal("new")
            .requires { it.sender is Player }
            .then(Commands.argument("type", ArgumentTypes.namespacedKey())
                .suggests { _, builder ->
                    Islands.PLAYER_KEYS.forEach {
                        builder.suggest(it.toString())
                    }
                    builder.buildFuture()
                }
                .executes { ctx ->
                    val player = ctx.source.sender as Player
                    val islandType = ctx.getArgument("type", NamespacedKey::class.java)
                    val groupId = dbManager.getPlayerGroupId(player.uniqueId.toString())!!

                    if(dbManager.getGroupIslands(groupId).isNotEmpty())
                    {
                        player.sendMessage(
                            Component.text("Your group already has a main island. Use '/island goto' to teleport to your group's island",
                                NamedTextColor.YELLOW)
                        )
                        return@executes 1
                    }

                    val island = islandManager.createIslandForGroup(groupId, islandType)
                    val location = island.location

                    val locationComponent = Component
                        .text("${location.blockX}, ${location.blockY}, ${location.blockZ}", NamedTextColor.AQUA)
                        .hoverEvent(HoverEvent.showText(Component.text("Click to teleport")))
                        .clickEvent(ClickEvent.suggestCommand("island goto"))

                    player.sendMessage(
                        Component.text("Your group's island was created at: ",
                            NamedTextColor.GREEN).append(locationComponent)
                    )
                    player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
                    0
                }
            )
        )
        .then(Commands.literal("goto")
            .requires { it.sender is Player }
            .executes { ctx ->
                val player = ctx.source.sender as Player
                val groupId = dbManager.getPlayerGroupId(player.uniqueId.toString()) ?: return@executes 1

                val island = dbManager.getGroupIslands(groupId).getOrNull(0)

                if(island == null)
                {
                    player.sendMessage(
                        Component.text("Your group has no islands created. Please use '/island new' to create one",
                            NamedTextColor.YELLOW)
                    )
                    return@executes 1
                }

                player.teleport(island.location)
                player.sendMessage(
                    Component.text("You has been teleported to your group's island.",
                        NamedTextColor.GREEN)
                )
                player.playSound(player.location, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f)
                0
            }
        ).build()
}