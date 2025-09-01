@file:Suppress("UNCHECKED_CAST")

package com.aletropy.skya.commands

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent

object CommandsManager
{
    fun register(commands :  ReloadableRegistrarEvent<Commands>)
    {
        val reg = commands.registrar()

        reg.register(GeneralCommands.REGEN)
        reg.register(GeneralCommands.SANDBOX)
        reg.register(GeneralCommands.GIVE)
        reg.register(UtilitiesCommands.GOTO)
        reg.register(IslandCommands.ISLAND)
        reg.register(GroupCommands.GROUP)
    }
}