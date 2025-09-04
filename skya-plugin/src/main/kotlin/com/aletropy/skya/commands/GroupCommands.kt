package com.aletropy.skya.commands

import com.aletropy.skya.Skya
import com.aletropy.skya.data.Messages
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound
import org.bukkit.entity.Player

object GroupCommands
{
    var dbManager = Skya.INSTANCE.dbManager
    var campfireManager = Skya.INSTANCE.campfireManager
    var groupManager = Skya.INSTANCE.groupManager
    var inviteManager = Skya.INSTANCE.inviteManager

    val GROUP: LiteralCommandNode<CommandSourceStack> = Commands.literal("group")
        .then(Commands.literal("leave")
            .requires { it.sender is Player }
            .executes { ctx ->

                val player = ctx.source.sender as Player
                val group = groupManager.getPlayerGroup(player) ?: return@executes 1

                if(groupManager.getGroupMembers(group.id).size < 2)
                {
                    player.sendMessage(Messages.LEAVING_FROM_SOLO_GROUP)
                    return@executes 0
                }

                groupManager.createGroupForPlayer(player)

                player.sendMessage(Messages.LEFT_FROM_GROUP_SELF)

                groupManager.broadcastMessage(group.id, Messages.LEFT_FROM_GROUP(player.name))
                0
            })
        .then(Commands.literal("rename")
            .requires { it.sender is Player }
            .then(Commands.argument("new_name", StringArgumentType.string())
                .executes { // group rename new_name
                    val newName = it.getArgument("new_name", String::class.java)
                    val player = it.source.sender as Player
                    val playerGroup = groupManager.getPlayerGroup(player) ?: return@executes 1
                    groupManager.renameGroup(playerGroup.id, newName)

                    dbManager.getGroupCampfires(playerGroup.id).forEach { campfire ->
                        campfireManager.updateCampfireDisplay(campfire.location, playerGroup.id)
                    }

                    player.sendMessage(Component.text("Your group has been renamed to ")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(newName).color(NamedTextColor.LIGHT_PURPLE)))
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                    0
                }))
        .then(Commands.literal("color")
            .requires { it.sender is Player }
            .then(Commands.argument("newColor", StringArgumentType.string())
                .suggests { ctx, builder ->
                    NamedTextColor.NAMES.keys().forEach {
                        builder.suggest(it)
                    }
                    builder.buildFuture()
                }
                .executes { // group color newColor
                    val newColorName = it.getArgument("newColor", String::class.java)
                    val newColor = NamedTextColor.NAMES.value(newColorName) ?: NamedTextColor.WHITE
                    val player = it.source.sender as Player
                    val playerGroup = groupManager.getPlayerGroup(player) ?: return@executes 1

                    groupManager.changeGroupColor(playerGroup.id, newColor)

                    dbManager.getGroupCampfires(playerGroup.id).forEach { campfire ->
                        campfireManager.updateCampfireDisplay(campfire.location, playerGroup.id)
                    }

                    player.sendMessage(Component.text("Your group has been colored to ")
                        .color(NamedTextColor.AQUA)
                        .append(Component.text(newColorName).color(newColor)))
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                    0
                }))
        .then(Commands.literal("invite")
            .requires { it.sender is Player }
            .then(Commands.argument("target", ArgumentTypes.player()).executes { ctx ->
                val targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java) as PlayerSelectorArgumentResolver
                val player = ctx.source.sender as Player
                val target = targetResolver.resolve(ctx.source).getOrNull(0) ?: return@executes 1
                inviteManager.invitePlayer(player, target)
                0
            }))
        .then(Commands.literal("accept")
            .requires { it.sender is Player }
            .executes { ctx ->
                inviteManager.acceptInvite(ctx.source.sender as Player)
                0
            })
        .then(Commands.literal("deny")
            .requires { it.sender is Player }
            .executes { ctx ->
                inviteManager.denyInvite(ctx.source.sender as Player)
                0
            })
        .build()

    val SE : LiteralCommandNode<CommandSourceStack> = Commands.literal("se")
        .requires { it.sender is Player }
        .executes { ctx ->
            val player = ctx.source.sender as Player
            val groupId = dbManager.getPlayerGroupId(player.uniqueId.toString())!!

            val se = groupManager.getGroupSkyEssence(groupId)

            player.sendMessage(
                Component.text("Your group has: ", NamedTextColor.AQUA).append(
                    Component.text("$se SE", NamedTextColor.LIGHT_PURPLE, TextDecoration.UNDERLINED)
                )
            )
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)

            0
        }.build()
}