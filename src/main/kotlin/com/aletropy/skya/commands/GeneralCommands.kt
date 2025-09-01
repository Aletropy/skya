package com.aletropy.skya.commands

import com.aletropy.skya.blocks.BlockManager
import com.aletropy.skya.blocks.CustomBlocks
import com.aletropy.skya.campfire.CampfireManager
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.group.GroupManager
import com.aletropy.skya.island.IslandManager
import com.aletropy.skya.island.Islands
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import java.util.*

@RegisterCommands
object GeneralCommands
{
    lateinit var dbManager : DatabaseManager
    lateinit var groupManager: GroupManager
    lateinit var campfireManager : CampfireManager
    lateinit var islandManager: IslandManager
    lateinit var blockManager: BlockManager

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

    val SANDBOX : LiteralCommandNode<CommandSourceStack> = Commands.literal("sandbox")
        .requires { it.sender is Player && it.sender.isOp }
        .executes { ctx ->
            val player = ctx.source.sender as Player
            val creator = WorldCreator("sandbox")

            val world = Bukkit.createWorld(creator) ?: return@executes 1

            val location = Location(world, 0.0, 180.0, 0.0)

            player.teleport(location)
            player.sendMessage(Component.text("You has been teleported to sandbox world!", NamedTextColor.GREEN))
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
            player.playSound(player.location, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f)

            0
        }.build()

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
}