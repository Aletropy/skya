package com.aletropy.skya

import com.aletropy.skya.campfire.CampfireManager
import com.aletropy.skya.commands.GeneralCommands
import com.aletropy.skya.commands.GroupCommands
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.listeners.CampfireListener
import com.aletropy.skya.listeners.PlayerEvents
import com.aletropy.skya.group.GroupManager
import com.aletropy.skya.group.InviteManager
import com.aletropy.skya.listeners.DataListener
import com.aletropy.skya.listeners.GroupListener
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Skya : JavaPlugin(), Listener
{
    lateinit var dbManager : DatabaseManager
    lateinit var groupManager: GroupManager
    lateinit var inviteManager: InviteManager
    lateinit var campfireManager : CampfireManager

    override fun onEnable() {
        dbManager = DatabaseManager(dataFolder)
        groupManager = GroupManager(dbManager)
        inviteManager = InviteManager(this, dbManager, groupManager)
        campfireManager = CampfireManager(dbManager)

        campfireManager.loadAllCampfires()

        GroupCommands.dbManager = dbManager
        GroupCommands.groupManager = groupManager
        GroupCommands.campfireManager = campfireManager
        GroupCommands.inviteManager = inviteManager

        GeneralCommands.groupManager = groupManager
        GeneralCommands.campfireManager = campfireManager
        GeneralCommands.dbManager = dbManager

        server.pluginManager.registerEvents(PlayerEvents(groupManager), this)
        server.pluginManager.registerEvents(CampfireListener(dbManager, campfireManager), this)
        server.pluginManager.registerEvents(GroupListener(groupManager), this)
        server.pluginManager.registerEvents(DataListener(dbManager), this)
    }

    override fun onDisable() {
        dbManager.close()
        inviteManager.shutdown()
    }
}