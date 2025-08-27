package com.aletropy.skya.data

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

object Messages
{
    val INVITE_SAME_GROUP =
        Component.text("The player already is in your group. Ignoring invite.")
            .color(NamedTextColor.GOLD)
    val INVITE_SELF =
        Component.text("You can't invite yourself.").color(NamedTextColor.RED)

    val NO_PENDING_INVITE =
        Component.text("You do not have any invites peding.", NamedTextColor.AQUA)

    val INVITE_SENT = { inviteeName : String -> Component
        .text("The invite was sent to $inviteeName")
        .color(NamedTextColor.GREEN) }

    val INVITE_RECEIVED = { inviterName : String, inviterGroupName : String ->
        Component.text("You received an invite from $inviterName to join $inviterGroupName",
            NamedTextColor.AQUA) }
    val INVITE_ACCEPTED_TARGET =
        Component.text("You accepted the invite!", NamedTextColor.GREEN)

    val INVITE_ACCEPTED = { inviteeName : String ->
        Component.text("$inviteeName has accepted your invite!", NamedTextColor.GREEN) }

    val INVITE_DENIED_TARGET =
        Component.text("You denied the invite!", NamedTextColor.RED)

    val INVITE_DENIED = { inviteeName : String ->
        Component.text("$inviteeName has denied your invite!", NamedTextColor.RED) }

    val LEFT_FROM_GROUP = { playerName : String ->
        Component.text("${playerName} left from group :(", NamedTextColor.DARK_PURPLE)
    }

    val LEFT_FROM_GROUP_SELF = Component.text("You left the group.", NamedTextColor.GREEN)

    val LEAVING_FROM_SOLO_GROUP = Component.text("You already in a solo group, you cannot leave.", NamedTextColor.YELLOW)
}