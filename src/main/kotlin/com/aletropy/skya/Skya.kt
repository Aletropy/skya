package com.aletropy.skya

import com.aletropy.skya.blocks.BlockManager
import com.aletropy.skya.campfire.CampfireManager
import com.aletropy.skya.commands.GeneralCommands
import com.aletropy.skya.commands.GroupCommands
import com.aletropy.skya.commands.IslandCommands
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.listeners.CampfireListener
import com.aletropy.skya.listeners.PlayerListener
import com.aletropy.skya.group.GroupManager
import com.aletropy.skya.group.InviteManager
import com.aletropy.skya.island.IslandManager
import com.aletropy.skya.listeners.BlocksListener
import com.aletropy.skya.listeners.DataListener
import com.aletropy.skya.listeners.GroupListener
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Skya : JavaPlugin(), Listener
{
    companion object {
        const val PLUGIN_ID = "skya"
        lateinit var INSTANCE : Skya
    }

    lateinit var dbManager : DatabaseManager
    lateinit var groupManager: GroupManager
    lateinit var inviteManager: InviteManager
    lateinit var campfireManager : CampfireManager
    lateinit var islandManager: IslandManager
    lateinit var blockManager: BlockManager

    init {
        INSTANCE = this
    }

    override fun onEnable() {
        dbManager = DatabaseManager(dataFolder)
        groupManager = GroupManager(dbManager)
        inviteManager = InviteManager(this, dbManager, groupManager)
        campfireManager = CampfireManager(this, dbManager)
        islandManager = IslandManager(dbManager, campfireManager)
        blockManager = BlockManager(this)

        campfireManager.loadAllCampfires()
        islandManager.loadAllIslands()
        blockManager.loadAllCustomBlocks()

        GroupCommands.dbManager = dbManager
        GroupCommands.groupManager = groupManager
        GroupCommands.campfireManager = campfireManager
        GroupCommands.inviteManager = inviteManager

        GeneralCommands.groupManager = groupManager
        GeneralCommands.campfireManager = campfireManager
        GeneralCommands.dbManager = dbManager
        GeneralCommands.islandManager = islandManager
        GeneralCommands.blockManager = blockManager

        IslandCommands.dbManager = dbManager
        IslandCommands.islandManager = islandManager

        server.pluginManager.registerEvents(PlayerListener(groupManager), this)
        server.pluginManager.registerEvents(CampfireListener(dbManager, campfireManager), this)
        server.pluginManager.registerEvents(GroupListener(groupManager), this)
        server.pluginManager.registerEvents(DataListener(dbManager), this)
        server.pluginManager.registerEvents(BlocksListener(blockManager), this)
    }

    override fun onDisable() {
        dbManager.close()
        inviteManager.shutdown()
    }
}