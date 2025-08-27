package com.aletropy.skya.group

import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.data.Group
import com.aletropy.skya.data.GroupMember
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

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

    fun broadcastMessage(groupId : Int, component : Component)
    {
        dbManager.getAllGroupMembers(groupId).forEach {
            Bukkit.getPlayer(UUID.fromString(it))?.sendMessage(component)
        }
    }

    fun createGroupForPlayer(player : Player)
    {
        val groupName = "${player.name}'s Team"
        val groupId = dbManager.createGroup(groupName)
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
        if(team == null) {
            team = scoreboard.registerNewTeam(teamName)
        }

        team.prefix(Component.text("[${groupName}] ").color(NamedTextColor.GRAY))
        team.addEntry(player.name)
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