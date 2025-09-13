package com.aletropy.skya

import BalanceConfig
import com.aletropy.skya.blocks.BlockManager
import com.aletropy.skya.campfire.CampfireManager
import com.aletropy.skya.commands.generated.CommandsRegistry
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.economy.SkyEssenceManager
import com.aletropy.skya.group.GroupManager
import com.aletropy.skya.group.InviteManager
import com.aletropy.skya.island.IslandManager
import com.aletropy.skya.listeners.ListenersRegistry
import com.aletropy.skya.menus.MenuManager
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
    lateinit var skyEssenceManager: SkyEssenceManager
    lateinit var inviteManager: InviteManager
    lateinit var campfireManager : CampfireManager
    lateinit var islandManager: IslandManager
    lateinit var blockManager: BlockManager

    init {
        INSTANCE = this
    }

    override fun onEnable() {

        BalanceConfig.load(this)
		MenuManager.initialize(this)

        dbManager = DatabaseManager(dataFolder)
        groupManager = GroupManager(dbManager)
        skyEssenceManager = SkyEssenceManager(this)
        inviteManager = InviteManager(this, dbManager, groupManager)
        campfireManager = CampfireManager(this, dbManager)
        islandManager = IslandManager(dbManager, campfireManager)
        blockManager = BlockManager(this)

        campfireManager.loadAllCampfires()
        islandManager.loadAllIslands()

        ListenersRegistry(this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            CommandsRegistry.registerAll(it.registrar())
        }
    }

    override fun onDisable() {
        dbManager.close()
        inviteManager.shutdown()
    }
}