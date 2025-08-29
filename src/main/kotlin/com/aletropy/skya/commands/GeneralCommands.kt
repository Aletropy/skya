package com.aletropy.skya.commands

import com.aletropy.skya.island.IslandManager
import com.aletropy.skya.campfire.CampfireManager
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.group.GroupManager
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import java.util.Random

object GeneralCommands
{
    lateinit var dbManager : DatabaseManager
    lateinit var groupManager: GroupManager
    lateinit var campfireManager : CampfireManager
    lateinit var islandManager: IslandManager

    val CREATE_ISLAND : LiteralCommandNode<CommandSourceStack> = Commands.literal("create-island")
        .requires { it.sender is Player }
        .executes { ctx ->
            val player = ctx.source.sender as Player
            val groupId = dbManager.getPlayerGroupId(player.uniqueId.toString())!!

            islandManager.createIslandForGroup(groupId)
            0
        }.build()

    val REGEN: LiteralCommandNode<CommandSourceStack> = Commands.literal("regen")
        .then(Commands.literal("island")
            .requires { it.sender is Player }
            .executes { context ->
                val player = context.source.sender as Player
                val world = player.world
                val location =  Location(world, 0.0, 100.0, 0.0)
                islandManager.generateNewIsland(location)

                world.getNearbyEntitiesByType(Item::class.java, location, 40.0).forEach {
                    it.remove()
                }

                player.playSound(player.location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f)
                player.sendMessage(Component.text("The island was regenerated.")
                    .color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))

                return@executes 1
            })
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

                location.block.type = Material.AIR

                player.playSound(player.location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f)
                player.sendMessage(Component.text("The lobby was regenerated.")
                    .color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                0
            }

            ).build()

}