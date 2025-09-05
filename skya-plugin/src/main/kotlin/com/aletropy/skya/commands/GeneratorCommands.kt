package com.aletropy.skya.commands

import BalanceConfig
import com.aletropy.skya.Skya
import com.aletropy.skya.blocks.custom.SkyEssenceGenerator
import com.aletropy.skya.data.TransactionReason
import com.aletropy.skya.economy.PassiveIncomeSource
import com.aletropy.skya.extensions.getGroupId
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player

object GeneratorCommands
{
	private val blockManager = Skya.INSTANCE.blockManager
	private val skyEssenceManager = Skya.INSTANCE.skyEssenceManager
	private val dbManager = Skya.INSTANCE.dbManager

	val GEN_COMMAND : LiteralCommandNode<CommandSourceStack> = Commands.literal("gen")
		.requires { it.sender is Player }
		.then(Commands.literal("upgrade")
			.then(Commands.argument("world", StringArgumentType.string())
				.then(Commands.argument("x", IntegerArgumentType.integer())
					.then(Commands.argument("y", IntegerArgumentType.integer())
						.then(Commands.argument("z", IntegerArgumentType.integer())
							.executes { ctx ->
								val player = ctx.source.sender as Player
								val world = Bukkit.getWorld(ctx.getArgument("world", String::class.java)) ?: return@executes 1
								val x = ctx.getArgument("x", Int::class.java)
								val y = ctx.getArgument("y", Int::class.java)
								val z = ctx.getArgument("z", Int::class.java)
								val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

								val customBlock = blockManager.blocks[location] as? SkyEssenceGenerator ?: return@executes 1

								val data = customBlock.getDataObject() as? SkyEssenceGenerator.Data ?: return@executes 1
								val playerGroupId = player.getGroupId()

								if(data.groupId != playerGroupId)
								{
									player.sendMessage(Component.text("This generator isn't yours.", NamedTextColor.RED))
									player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f)
									return@executes 1
								}

								val cost = BalanceConfig.getGeneratorUpgradeCost(data.level)
								if (!skyEssenceManager.removeEssence(data.groupId, cost.toInt(), TransactionReason.BLOCK_UPGRADE)) {
									player.sendMessage(Component.text("Seu grupo não tem SE suficiente!", NamedTextColor.RED))
									player.playSound(player.location, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f)
									return@executes 1
								}

								data.level++
								blockManager.saveBlockState(location, customBlock)

								val newProduction = BalanceConfig.getGeneratorProduction(data.level)
								skyEssenceManager.registerPassiveSource(data.groupId, PassiveIncomeSource(location, newProduction))

								player.sendMessage(Component.text("Generator evolved to level ${data.level}!", NamedTextColor.GREEN))
								world.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f)
								world.spawnParticle(Particle.TOTEM_OF_UNDYING, location.toCenterLocation().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5)

								0
							}
						)
					)
				)
			)
		).build()
}