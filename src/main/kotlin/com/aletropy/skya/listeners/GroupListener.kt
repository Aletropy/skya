package com.aletropy.skya.listeners

import com.aletropy.skya.group.GroupManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class GroupListener(private val groupManager: GroupManager) : Listener
{
    @EventHandler
    fun onPlayerJoin(event : PlayerJoinEvent)
    {
        val player = event.player
        val group = groupManager.getPlayerGroup(player)
        if(group == null)
            groupManager.createGroupForPlayer(player)
        else
            groupManager.updatePlayerTeamPrefix(player, group.name)
    }
}