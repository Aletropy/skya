package com.aletropy.skya.events

import com.aletropy.skya.data.BoundCampfire
import com.aletropy.skya.data.Island
import org.bukkit.event.Event
import org.bukkit.event.HandlerList


class IslandCreatedEvent(val island: Island) : Event()
{
    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList() : HandlerList {
            return HANDLER_LIST
        }
    }

    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }
}