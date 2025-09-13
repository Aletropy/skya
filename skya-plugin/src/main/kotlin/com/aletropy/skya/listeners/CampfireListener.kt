package com.aletropy.skya.listeners

import com.aletropy.skya.Skya
import com.aletropy.skya.campfire.CampfireMenu
import com.aletropy.skya.extensions.getGroupId
import com.aletropy.skya.extensions.playSound
import com.aletropy.skya.island.SKY_CAMPFIRE_KEY
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.block.Campfire
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

@RegisterListener
class CampfireListener : Listener
{
	private val dbManager = Skya.INSTANCE.dbManager
	private val campfireManager = Skya.INSTANCE.campfireManager

	@EventHandler
	fun onChunkLoad(event: ChunkLoadEvent)
	{
		Bukkit.getScheduler().runTaskAsynchronously(Skya.INSTANCE, Runnable {
			val campfiresInChunk = dbManager.getCampfiresInChunk(event.chunk)

			Bukkit.getScheduler().runTask(Skya.INSTANCE, Runnable{
				campfiresInChunk.forEach { campfire ->
					campfireManager.createDisplaysForCampfire(campfire.location, campfire.groupId)
				}
			})
		})
	}

	@EventHandler
	fun onChunkUnloaded(event : ChunkUnloadEvent)
	{
		val chunk = event.chunk
		campfireManager.clearDisplaysFromChunk(chunk)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onCampfireBroken(event: BlockBreakEvent)
	{
		if (event.block.type != Material.CAMPFIRE && event.block.type != Material.SOUL_CAMPFIRE) return

		val player = event.player
		val breakerGroupId = dbManager.getPlayerGroupId(player.uniqueId.toString())
		val boundCampfire = campfireManager.getCampfire(event.block.location) ?: return

		if (boundCampfire.groupId == breakerGroupId && player.gameMode == GameMode.SURVIVAL)
		{
			event.isCancelled = true
			player.sendMessage(Component.text("You cannot destroy your own group campfire!").color(NamedTextColor.RED))
			player.playSound(player.location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f)
			return
		}

		campfireManager.removeCampfire(event.block.location)
	}


	@EventHandler
	fun onCampfireExploded(event: BlockExplodeEvent)
	{
		if (event.explodedBlockState.type != Material.CAMPFIRE && event.explodedBlockState.type != Material.SOUL_CAMPFIRE) return
		campfireManager.removeCampfire(event.block.location)
	}

	@EventHandler
	fun onPlayerInteractWithCampfire(event: PlayerInteractEvent)
	{
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		val clickedBlock = event.clickedBlock ?: return
		if (clickedBlock.type != Material.CAMPFIRE && clickedBlock.type != Material.SOUL_CAMPFIRE) return

		if (clickedBlock.state is Campfire)
		{
			val state = clickedBlock.state as Campfire
			val pdc = state.persistentDataContainer

			if (!pdc.has(SKY_CAMPFIRE_KEY))
				return
		}

		val location = clickedBlock.location
		val boundCampfire = dbManager.getBoundCampfire(location)

		if (boundCampfire != null)
		{
			if(boundCampfire.groupId != event.player.getGroupId())
			{
				event.player.playSound(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO)
				event.player.spawnParticle(
					Particle.ANGRY_VILLAGER, location.clone().add(0.0, 1.0, 0.0),
					10, .1, .1, .05
				)
				event.player.sendMessage(
					Component.text("You can't interact with other groups campfire.", NamedTextColor.RED)
				)
				return
			}

			CampfireMenu(event.player, boundCampfire).open()
			return
		}

		val player = event.player
		val group = dbManager.getPlayerGroup(player.uniqueId.toString()) ?: return

		campfireManager.bindCampfireToGroup(location, group.id)
		player.sendMessage(
			Component.text("This campfire is now bound to ${group.name}!")
				.color(NamedTextColor.GREEN)
		)
		player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
	}

}