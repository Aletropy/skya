package com.aletropy.skya.shop

import com.aletropy.skya.Skya
import com.aletropy.skya.api.util.ItemBuilder
import com.aletropy.skya.blocks.custom.SkyEssenceGenerator
import com.aletropy.skya.campfire.CampfireMenu
import com.aletropy.skya.data.BoundCampfire
import com.aletropy.skya.data.TransactionReason
import com.aletropy.skya.extensions.getGroupId
import com.aletropy.skya.extensions.playSound
import com.aletropy.skya.menus.Arrangement
import com.aletropy.skya.menus.Menu
import com.aletropy.skya.menus.Rows
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player

class ShopMenu(player: Player, private val boundCampfire : BoundCampfire, currentPage: Int = 0) : Menu(Skya.INSTANCE, player, currentPage)
{
	override fun build()
	{
		val groupId = player.getGroupId() ?: return

		title = Component.text("Sky Essence Shop", NamedTextColor.DARK_GREEN,
			TextDecoration.BOLD, TextDecoration.UNDERLINED)
		size = Rows.SIX

		val glassPane = ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
			.name(Component.text(" ")).build()
		fill(glassPane)

		slot(
			11, ItemBuilder(Material.BUDDING_AMETHYST)
				.name(Component.text("Sky Essence Generator", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
				.description(
					Component.text("Cost: 1000/SE", NamedTextColor.GREEN, TextDecoration.BOLD),
							Component.text("Generates passive Sky Essence.", NamedTextColor.GRAY)
				)
				.enchantmentGlint(true)
				.build()
		) {

			if(
				!Skya.INSTANCE.skyEssenceManager.removeEssence(groupId, 1000, TransactionReason.SHOP_PURCHASE))
			{
				player.playSound(Sound.ENTITY_VILLAGER_NO)
				player.sendMessage(
					Component.text("Your group has insufficient funds.", NamedTextColor.RED)
				)
				return@slot
			}

			player.playSound(Sound.UI_BUTTON_CLICK)
			player.playSound(Sound.ENTITY_VILLAGER_TRADE)

			boundCampfire.location.world.dropItemNaturally(
				boundCampfire.location.clone().add(0.5, 1.5, 0.5), SkyEssenceGenerator.STACK
			)

			boundCampfire.location.world.spawnParticle(
				Particle.TOTEM_OF_UNDYING, boundCampfire.location,
				30, .15, .15, .15, .1
			)

			player.closeInventory()

			Skya.INSTANCE.groupManager.broadcastMessage(groupId,
				Component.text("${player.name} has purchased a Sky Essence Generator.", NamedTextColor.GRAY)
			)
		}

		slot(Arrangement.BOTTOM_RIGHT.getSlot(size), ItemBuilder(Material.BARRIER).name(Component.text("<- Back", NamedTextColor.RED)).build()) {
			it.player.playSound(Sound.BLOCK_CHEST_CLOSE, pitch = 1.5f)
			CampfireMenu(player, boundCampfire).open()
		}
	}
}