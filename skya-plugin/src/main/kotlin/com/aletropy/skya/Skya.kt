package com.aletropy.skya

import com.aletropy.skya.blocks.BlockManager
import com.aletropy.skya.campfire.CampfireManager
import com.aletropy.skya.commands.GeneralCommands
import com.aletropy.skya.commands.GroupCommands
import com.aletropy.skya.commands.IslandCommands
import com.aletropy.skya.commands.UtilitiesCommands
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.group.GroupManager
import com.aletropy.skya.group.InviteManager
import com.aletropy.skya.island.IslandManager
import com.aletropy.skya.listeners.ListenersRegistry
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
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

        ListenersRegistry(this)
        registerCommands()
    }

    fun registerCommands()
    {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            val reg = commands.registrar()
            reg.register(GeneralCommands.GIVE)
            reg.register(GeneralCommands.REGEN)
            reg.register(GroupCommands.GROUP)
            reg.register(GroupCommands.SE)
            reg.register(IslandCommands.ISLAND)
            reg.register(UtilitiesCommands.GOTO)
        }
    }

    override fun onDisable() {
        dbManager.close()
        inviteManager.shutdown()
    }
}