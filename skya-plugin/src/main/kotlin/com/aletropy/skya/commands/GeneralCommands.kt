package com.aletropy.skya.commands

import com.aletropy.skya.Skya
import com.aletropy.skya.api.CommandsProvider
import com.aletropy.skya.blocks.custom.CustomBlocks
import com.aletropy.skya.island.Islands
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import java.util.*

@CommandsProvider
object GeneralCommands
{
    var dbManager = Skya.INSTANCE.dbManager
    var campfireManager = Skya.INSTANCE.campfireManager
    var groupManager = Skya.INSTANCE.groupManager

    var islandManager = Skya.INSTANCE.islandManager
    var blockManager = Skya.INSTANCE.blockManager

    val REGEN: LiteralCommandNode<CommandSourceStack> = Commands.literal("regen")
        .requires { it.sender.isOp }
        .then(Commands.literal("island")
            .requires { it.sender is Player }
            .then(Commands.argument("type", ArgumentTypes.namespacedKey())
                .suggests { _, builder ->
                    Islands.PLAYER_KEYS.forEach {
                        builder.suggest(it.toString())
                    }
                    builder.buildFuture()
                }
                .executes { context ->
                    val player = context.source.sender as Player
                    val islandType = context.getArgument("type", NamespacedKey::class.java)
                    val world = player.world
                    val location =  Location(world, 0.0, 100.0, 0.0)
                    islandManager.generateNewIsland(location, islandType)

                    world.getNearbyEntitiesByType(Item::class.java, location, 40.0).forEach {
                        it.remove()
                    }

                    player.playSound(player.location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f)
                    player.sendMessage(Component.text("The island was regenerated.")
                        .color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))

                    return@executes 1
                }
            ))
        .then(Commands.literal("lobby")
            .requires { it.sender is Player }
            .executes { ctx ->
                val player = ctx.source.sender as Player
                val world = Bukkit.getWorld("world") ?: return@executes 1

                val structureManager = Bukkit.getStructureManager()
                val lobbyStructure = structureManager.loadStructure(NamespacedKey.minecraft("lobby")) ?: return@executes 1

                val location = Location(world, 9999.0, 100.0, 9999.0)

                lobbyStructure.place(
                location.clone().add(-12.0, -5.0, -12.0), true, StructureRotation.NONE, Mirror.NONE,
                    -1, 1.0f, Random()
                )

                player.playSound(player.location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f)
                player.sendMessage(Component.text("The lobby was regenerated.")
                    .color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                0
            }

            ).build()

    val GIVE: LiteralCommandNode<CommandSourceStack> = Commands.literal("sgive")
        .requires { it.sender is Player && it.sender.isOp }
        .then(Commands.literal("block")
            .then(Commands.argument("id", StringArgumentType.greedyString())
                .suggests { ctx, builder ->
                    CustomBlocks.BLOCKS.forEach {
                        builder.suggest(it.key)
                    }
                    builder.buildFuture()
                }
                .executes { ctx ->
                    val customBlockId = ctx.getArgument("id", String::class.java)
                    val customBlockClass = CustomBlocks.BLOCKS[customBlockId]!!

                    val customBlockStack = customBlockClass.constructors.first().call().stack

                    val player = ctx.source.sender as Player
                    player.give(customBlockStack)
                    0
                }
            )
        ).build()

    val BOX : LiteralCommandNode<CommandSourceStack> = Commands.literal("box")
        .executes {
            val player = it.source.sender as Player
            val border = Bukkit.createWorldBorder()

            border.setCenter(0.0, 0.0)
            border.size = 16.0

            player.worldBorder = border
            0
        }.build()
}