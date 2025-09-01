package com.aletropy.skya.listeners

import com.aletropy.skya.Skya
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

@RegisterListener
class GroupListener : Listener
{
    private val groupManager = Skya.INSTANCE.groupManager

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