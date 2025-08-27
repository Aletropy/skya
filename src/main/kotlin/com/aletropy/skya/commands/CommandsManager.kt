package com.aletropy.skya.commands

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent

object CommandsManager
{
    fun register(commands :  ReloadableRegistrarEvent<Commands>)
    {
        val reg = commands.registrar()

        reg.register(GeneralCommands.REGEN_ISLAND)
        reg.register(GeneralCommands.RELOAD)
        reg.register(GroupCommands.GROUP)
    }
}