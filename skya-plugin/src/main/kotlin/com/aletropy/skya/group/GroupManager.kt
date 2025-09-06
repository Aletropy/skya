package com.aletropy.skya.group

import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.data.Group
import com.aletropy.skya.data.GroupMember
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team
import java.util.*

class GroupManager(private val dbManager : DatabaseManager)
{
    fun getPlayerGroup(player : Player) : Group?
    {
        val groupId = dbManager.getPlayerGroupId(player.uniqueId.toString()) ?: return null
        return dbManager.getGroupById(groupId)
    }

    fun getGroupMembers(groupId : Int) : List<GroupMember>
    {
        val list = mutableListOf<GroupMember>()
        dbManager.getAllGroupMembers(groupId).forEach {
            list.add(GroupMember(it, groupId))
        }
        return list
    }

    fun getGroupSkyEssence(groupId: Int) : Int
    {
        return dbManager.getGroupById(groupId)?.skyEssence ?: 0
    }


    fun broadcastMessage(groupId : Int, component : Component)
    {
        dbManager.getAllGroupMembers(groupId).forEach {
            Bukkit.getPlayer(UUID.fromString(it))?.sendMessage(component)
        }
    }

    fun createGroupForPlayer(player : Player)
    {
        val randomColor = run {
            val keys = NamedTextColor.NAMES.keys()
            NamedTextColor.NAMES.value(keys.random()) ?: NamedTextColor.WHITE
        }

        val groupName = "${player.name}'s Team"
        val groupId = dbManager.createGroup(groupName, randomColor.value())
        if(groupId != -1) {
            dbManager.addPlayerToGroup(player.uniqueId.toString(), groupId)
            updatePlayerTeamPrefix(player, groupName)
        }
    }

    fun updatePlayerTeamPrefix(player : Player, groupName : String)
    {
        val scoreboard = player.scoreboard
        val teamName = "group_${groupName.replace(" ", "_")}"
        var team = scoreboard.getTeam(teamName)

        val groupColorValue = dbManager.getPlayerGroup(player.uniqueId.toString())?.color ?: NamedTextColor.WHITE.value()
        val color = NamedTextColor.namedColor(groupColorValue)

        if(team == null) {
            team = scoreboard.registerNewTeam(teamName)
        }

        team.setOption(
            Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM
        )

        team.setOption(
            Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS
        )

        team.setAllowFriendlyFire(false)

        team.prefix(Component.text("[${groupName}] ").color(color))
        team.color(color)
        team.addEntry(player.name)
    }

    fun changeGroupColor(groupId : Int, newColor : NamedTextColor)
    {
        val group = dbManager.getGroupById(groupId) ?: return
        group.color = newColor.value()

        dbManager.updateGroup(group)

        dbManager.getAllGroupMembers(groupId).forEach { uuid ->
            val player = Bukkit.getPlayer(UUID.fromString(uuid))
            player?.let {
                updatePlayerTeamPrefix(it, group.name)
            }
        }
    }

    fun renameGroup(groupId: Int, newName: String)
    {
        val group = dbManager.getGroupById(groupId) ?: return
        group.name = newName

        dbManager.getAllGroupMembers(groupId).forEach { uuid ->
            val player = Bukkit.getPlayer(UUID.fromString(uuid))
            player?.let {
                updatePlayerTeamPrefix(it, newName)
            }
        }

        dbManager.updateGroup(group)
    }

}