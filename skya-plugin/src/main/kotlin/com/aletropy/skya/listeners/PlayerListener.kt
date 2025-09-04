package com.aletropy.skya.listeners

import com.aletropy.skya.Skya
import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@RegisterListener
class PlayerListener() : Listener
{
    private val dbManager = Skya.INSTANCE.dbManager
    private val groupManager = Skya.INSTANCE.groupManager

    @EventHandler
    fun onInventoryChanged(event : PlayerInventorySlotChangeEvent)
    {
        if(event.newItemStack.type != Material.AMETHYST_SHARD) return

        val amount = event.newItemStack.amount

        val player = event.player
        val groupId = dbManager.getPlayerGroupId(player.uniqueId.toString())!!

        groupManager.broadcastMessage(groupId,
            Component.text("${player.name} has consumed $amount amethyst shards.", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(" +${amount*1000}SE", NamedTextColor.AQUA, TextDecoration.ITALIC)))

        dbManager.addSkyEssenceToGroup(groupId, amount * 1000)
        player.inventory.remove(Material.AMETHYST_SHARD)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(event : AsyncChatEvent)
    {
        val group = groupManager.getPlayerGroup(event.player) ?: return

        val prefix = Component.text("[${group.name}] ", NamedTextColor.namedColor(group.color))

        event.signedMessage()

        event.renderer { _, _, _, _ ->
            Component.text("<")
                .append(prefix)
                .appendSpace()
                .append(Component.text(event.player.name))
                .append(Component.text(">"))
                .appendSpace()
                .append(event.originalMessage())
        }
    }
}