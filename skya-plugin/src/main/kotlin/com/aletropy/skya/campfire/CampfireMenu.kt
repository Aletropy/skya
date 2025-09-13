package com.aletropy.skya.campfire

import com.aletropy.skya.Skya
import com.aletropy.skya.api.util.ItemBuilder
import com.aletropy.skya.data.BoundCampfire
import com.aletropy.skya.data.TransactionReason
import com.aletropy.skya.extensions.getGroupId
import com.aletropy.skya.extensions.playSound
import com.aletropy.skya.menus.Arrangement
import com.aletropy.skya.menus.Menu
import com.aletropy.skya.menus.Rows
import com.aletropy.skya.shop.ShopMenu
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

class CampfireMenu(player: Player, private val campfire: BoundCampfire) : Menu(Skya.INSTANCE, player)
{
	override fun build()
	{
		val group = Skya.INSTANCE.groupManager.getPlayerGroup(player) ?: return

		val passiveIncome = Skya.INSTANCE.skyEssenceManager.getPassiveIncomeForGroup(group.id)

		title(
			Component.text(
				"${group.name}'s Campfire",
				NamedTextColor.namedColor(group.color),
				TextDecoration.BOLD, TextDecoration.UNDERLINED
			)
		)
		size(Rows.ONE)

		fill(ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).name(Component.space()).build())

		slot(
			Arrangement.CENTER.getSlot(size) - 2,
			ItemBuilder(Material.AMETHYST_SHARD)
				.name(
					Component.text(
						"Producing: $passiveIncome SE/s",
						NamedTextColor.LIGHT_PURPLE,
						TextDecoration.BOLD
					)
				)
				.description(Component.text(
					"Click to consume all your amethyst shards.",
					NamedTextColor.DARK_GRAY,
					TextDecoration.BOLD
					)
				)
				.enchantmentGlint(true)
				.build()
		) {
			if(!player.inventory.contains(Material.AMETHYST_SHARD))
			{
				player.playSound(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO)
				player.sendMessage(Component.text(
					"You don't have any amethyst shards.",
					NamedTextColor.RED
				))
				return@slot
			}

			var amount = 0

			for ((_, stack) in player.inventory.all(Material.AMETHYST_SHARD))
				amount += stack.amount

			val groupId = player.getGroupId() ?: return@slot

			Skya.INSTANCE.groupManager.broadcastMessage(
				groupId,
				Component.text("${player.name} has consumed $amount amethyst shards.", NamedTextColor.LIGHT_PURPLE)
					.append(Component.text(" +${amount * 1000}SE", NamedTextColor.AQUA, TextDecoration.ITALIC))
			)

			Skya.INSTANCE.skyEssenceManager.addEssence(groupId, amount, TransactionReason.PLAYER_CONSUME)
			player.inventory.remove(Material.AMETHYST_SHARD)
		}

		slot(
			Arrangement.CENTER.getSlot(size),
			ItemBuilder(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
				.name(Component.text("Upgrade Island", NamedTextColor.GREEN, TextDecoration.BOLD))
				.description(
					Component.text("Level: ", NamedTextColor.GRAY).append(
						Component.text("0", NamedTextColor.DARK_GRAY)
					)
				)
				.hideTooltip(true)
				.build()
		) {
			player.playSound(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO)
		}

		slot(
			Arrangement.CENTER.getSlot(size) + 2,
			ItemBuilder(Material.WANDERING_TRADER_SPAWN_EGG)
				.name(Component.text("SE Shop", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
				.enchantmentGlint(true)
				.build()
		) {
			player.playSound(Sound.BLOCK_NOTE_BLOCK_BELL)
			ShopMenu(player, campfire).open()
		}
	}
}