package com.aletropy.skya.extensions

import com.aletropy.skya.Skya
import org.bukkit.entity.Player

fun Player.getGroupId() : Int? {
	try {
		return Skya.INSTANCE.dbManager.getPlayerGroupId(uniqueId.toString())
	} catch (e : Exception) {
		e.printStackTrace()
		return null
	}
}