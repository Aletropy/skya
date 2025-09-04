package com.aletropy.skya.listeners

import com.aletropy.skya.Skya
import com.aletropy.skya.events.BoundCampfireRemovedEvent
import com.aletropy.skya.events.BoundedCampfireEvent
import com.aletropy.skya.events.IslandCreatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

@RegisterListener
class DataListener : Listener
{
    private val dbManager = Skya.INSTANCE.dbManager

    @EventHandler
    fun onBoundedCampfire(event : BoundedCampfireEvent)
    {
        val campfire = event.campfire
        dbManager.storeBindedCampfire(campfire.location, campfire.groupId)
    }

    @EventHandler
    fun onBoundCampfireRemoved(event : BoundCampfireRemovedEvent)
    {
        dbManager.removeBindedCampfire(event.location)
    }

    @EventHandler
    fun onIslandCreated(event : IslandCreatedEvent)
    {
        dbManager.storeIsland(event.island)
    }
}