package com.aletropy.skya.listeners

import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.events.BoundCampfireRemovedEvent
import com.aletropy.skya.events.BoundedCampfireEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class DataListener(private val dbManager: DatabaseManager) : Listener
{
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
}