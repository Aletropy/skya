package com.aletropy.skya.listeners

import com.aletropy.skya.campfire.CampfireManager
import com.aletropy.skya.data.DatabaseManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent

class CampfireListener(private val dbManager : DatabaseManager, private val campfireManager: CampfireManager) : Listener
{
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCampfireBroken(event : BlockBreakEvent) {
        if(event.block.type != Material.CAMPFIRE && event.block.type != Material.SOUL_CAMPFIRE) return

        val player = event.player
        val breakerGroupId = dbManager.getPlayerGroupId(player.uniqueId.toString())
        val boundCampfire = campfireManager.getCampfire(event.block.location) ?: return

        if(boundCampfire.groupId == breakerGroupId && player.gameMode == GameMode.SURVIVAL)
        {
            event.isCancelled = true
            player.sendMessage(Component.text("You cannot destroy your own group campfire!").color(NamedTextColor.RED))
            player.playSound(player.location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f)
            return
        }

        campfireManager.removeCampfire(event.block.location)
    }

    @EventHandler
    fun onCampfireExploded(event : BlockExplodeEvent) {
        if(event.block.type != Material.CAMPFIRE && event.block.type != Material.SOUL_CAMPFIRE) return
        campfireManager.removeCampfire(event.block.location)
    }

    @EventHandler
    fun onPlayerInteractWithCampfire(event : PlayerInteractEvent)
    {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock.type != Material.CAMPFIRE && clickedBlock.type != Material.SOUL_CAMPFIRE) return

        val location = clickedBlock.location
        val boundCampfire = dbManager.getBoundCampfire(location)
        if (boundCampfire != null) return

        val player = event.player
        val group = dbManager.getPlayerGroup(player.uniqueId.toString()) ?: return

        campfireManager.bindCampfireToGroup(location, group.id)
        player.sendMessage(Component.text("This campfire is now bound to ${group.name}!")
            .color(NamedTextColor.GREEN))
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
    }

}