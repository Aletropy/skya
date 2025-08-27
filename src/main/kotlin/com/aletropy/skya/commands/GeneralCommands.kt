package com.aletropy.skya.commands

import com.aletropy.skya.campfire.CampfireManager
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.group.GroupManager
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Particle
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

    val REGEN_ISLAND: LiteralCommandNode<CommandSourceStack> = Commands.literal("regen-island")
        .requires { it.sender is Player }
        .executes { context ->
            val player = context.source.sender as Player
            val world = player.world

            val structureManager = Bukkit.getStructureManager()
            val structureKey = NamespacedKey.minecraft("island")

            val structure = structureManager.loadStructure(structureKey, false)
            if (structure == null) {
                player.sendMessage(Component.text("[FALHA] A estrutura '$structureKey' não foi encontrada! Verifique o nome e a pasta do arquivo.", NamedTextColor.RED))
                return@executes 0
            }

            val finalX = 0.0 - 7
            val finalY = 100.0 - 11
            val finalZ = 0.0 - 7
            val placementLocation = Location(world, finalX, finalY, finalZ)

            structure.place(placementLocation,
                true,
                StructureRotation.NONE,
                Mirror.NONE,
                -1, 1.0f, Random()
                )

            world.getNearbyEntitiesByType(Item::class.java, Location(world, 0.0, 100.0, 0.0), 40.0).forEach {
                it.remove()
            }

            val minX = 93.0
            val minY = 88.0
            val minZ = 93.0
            val maxX = 107.0
            val maxY = 112.0
            val maxZ = 107.0

            for (x in minX.toInt()..maxX.toInt()) {
                for (y in minY.toInt()..maxY.toInt()) {
                    for (z in minZ.toInt()..maxZ.toInt()) {
                        if (Random().nextInt(4) == 0) {
                            val particleLocation = Location(world, x + 0.5, y + 0.5, z + 0.5)
                            world.spawnParticle(Particle.HAPPY_VILLAGER, particleLocation, 10)
                        }
                    }
                }
            }

            player.playSound(player.location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f)
            player.sendMessage(Component.text("The island was regenerated.")
                .color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))

            return@executes 1
        }.build()

    val RELOAD : LiteralCommandNode<CommandSourceStack> = Commands.literal("reload")
        .requires { it.sender.isOp }
        .executes {
                dbManager.getAllBoundCampfires().forEach {
                    campfireManager.updateCampfireDisplay(it.location, it.groupId)
                }
            0
        }.build()
}