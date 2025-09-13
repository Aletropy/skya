package com.aletropy.skya.menus

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.java.JavaPlugin

object MenuManager
{
	fun initialize(plugin : JavaPlugin) {
		Bukkit.getPluginManager().registerEvents(MenuListener, plugin)
	}

	private object MenuListener : Listener
	{
		@EventHandler
		fun onInventoryClicked(event : InventoryClickEvent)
		{
			val holder = event.inventory.holder

			if(holder is Menu) {
				event.isCancelled = true
				holder.handleClick(event.rawSlot, event.click)
			}
		}
	}
}