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
        reg.register(GeneralCommands.CREATE_ISLAND)
        reg.register(UtilitiesCommands.GOTO)
        reg.register(GroupCommands.GROUP)
    }
}