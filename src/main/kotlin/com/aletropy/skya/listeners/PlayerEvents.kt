package com.aletropy.skya.listeners

import com.aletropy.skya.campfire.CampfireManager
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.group.GroupManager
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent

class PlayerEvents(private val groupManager : GroupManager) : Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(event : AsyncChatEvent)
    {
        val group = groupManager.getPlayerGroup(event.player) ?: return

        val prefix = Component.text("[${group.name}]", NamedTextColor.GRAY)

        event.signedMessage()

        event.message(
            Component.text("<")
                .append(prefix)
                .appendSpace()
                .append(Component.text(event.player.name))
                .append(Component.text(">"))
                .appendSpace()
                .append(event.originalMessage())
        )
    }
}