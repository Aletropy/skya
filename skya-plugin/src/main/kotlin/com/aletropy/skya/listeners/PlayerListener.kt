package com.aletropy.skya.listeners

import com.aletropy.skya.Skya
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@RegisterListener
class PlayerListener() : Listener
{
    private val dbManager = Skya.INSTANCE.dbManager
    private val groupManager = Skya.INSTANCE.groupManager

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(event : AsyncChatEvent)
    {
        val group = groupManager.getPlayerGroup(event.player) ?: return

        val prefix = Component.text(" [${group.name}] ", NamedTextColor.namedColor(group.color))

        event.signedMessage()

        event.renderer { _, _, _, _ ->
            Component.text("< ")
                .append(prefix)
                .appendSpace()
                .append(Component.text(event.player.name))
                .append(Component.text(">"))
                .appendSpace()
                .append(event.originalMessage())
        }
    }
}