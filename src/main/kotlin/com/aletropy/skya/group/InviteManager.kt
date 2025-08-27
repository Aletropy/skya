package com.aletropy.skya.group

import com.aletropy.skya.Skya
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.data.GroupInvite
import com.aletropy.skya.data.Messages
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InviteManager(
    private val plugin : Skya,
    private val dbManager : DatabaseManager,
    private val groupManager: GroupManager
)
{
    companion object {
        private const val INVITE_TIMEOUT_SECONDS = 60L
    }
    val invites = ConcurrentHashMap<UUID, GroupInvite>()

    private val invitesCleanupTask : BukkitTask =
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {

            val now = System.currentTimeMillis()
            invites.entries.removeIf {
                (now - it.value.timestamp) / 1000 > INVITE_TIMEOUT_SECONDS
            }

    }, 20L * 10, 20L * 5)

    fun shutdown()
    {
        invitesCleanupTask.cancel()
    }

    fun invitePlayer(inviter : Player, invitee : Player)
    {
        val inviterGroup = dbManager.getPlayerGroup(inviter.uniqueId.toString()) ?: return

        if(!checkInviteErrors(inviter, invitee))
            return

        val invite = GroupInvite(
            inviter.uniqueId,
            inviterGroup.id,
            inviterGroup.name
        )

        invites[invitee.uniqueId] = invite

        inviter.sendMessage(Messages.INVITE_SENT(invitee.name))

        val acceptComponent =
            Component.text("[ACCEPT]", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/group accept"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to join ${inviterGroup.name}")))

        val denyComponent =
            Component.text("[DENY]", NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand("/group deny"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to deny ${inviterGroup.name} invite.")))

        invitee.sendMessage(Messages.INVITE_RECEIVED(inviter.name, inviterGroup.name))
        invitee.sendMessage(Component.text()
            .append(acceptComponent)
            .appendSpace()
            .append(denyComponent))
    }

    fun checkInviteErrors(inviter : Player, invitee : Player) : Boolean
    {
        val inviterGroupId = dbManager.getPlayerGroupId(inviter.uniqueId.toString())
        val inviteeGroupId = dbManager.getPlayerGroupId(invitee.uniqueId.toString())

        if(inviterGroupId == inviteeGroupId)
        {
            inviter.sendMessage(Messages.INVITE_SAME_GROUP)
            return false
        }

        if(inviter.uniqueId == invitee.uniqueId)
        {
            inviter.sendMessage(Messages.INVITE_SELF)
            return false
        }

        return true
    }

    fun acceptInvite(player : Player)
    {
        val invite = invites.remove(player.uniqueId)
        if(invite == null) {
            player.sendMessage(Messages.NO_PENDING_INVITE)
            return
        }

        dbManager.addPlayerToGroup(player.uniqueId.toString(), invite.groupId)
        groupManager.updatePlayerTeamPrefix(player, invite.inviterGroupName)

        player.sendMessage(Messages.INVITE_ACCEPTED_TARGET)

        Bukkit.getPlayer(invite.inviterUUID)?.sendMessage(Messages.INVITE_ACCEPTED(player.name))
    }

    fun denyInvite(player : Player)
    {
        val invite = invites.remove(player.uniqueId)
        if(invite == null) {
            player.sendMessage(Messages.NO_PENDING_INVITE)
            return
        }

        player.sendMessage(Messages.INVITE_DENIED_TARGET)

        Bukkit.getPlayer(invite.inviterUUID)?.sendMessage(Messages.INVITE_DENIED(player.name))
    }
}