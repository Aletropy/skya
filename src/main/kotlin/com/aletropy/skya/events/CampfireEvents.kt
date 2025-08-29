package com.aletropy.skya.events

import com.aletropy.skya.data.BoundCampfire
import org.bukkit.Location
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class BoundedCampfireEvent(val campfire : BoundCampfire) : Event()
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

class BoundCampfireUpdateEvent(val campfire : BoundCampfire) : Event()
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

class BoundCampfireRemovedEvent(val location : Location) : Event()
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