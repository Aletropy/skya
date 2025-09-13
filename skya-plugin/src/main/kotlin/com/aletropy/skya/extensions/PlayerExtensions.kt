package com.aletropy.skya.extensions

import com.aletropy.skya.Skya
import org.bukkit.Sound
import org.bukkit.entity.Player

fun Player.getGroupId() : Int? {
	try {
		return Skya.INSTANCE.dbManager.getPlayerGroupId(uniqueId.toString())
	} catch (e : Exception) {
		e.printStackTrace()
		return null
	}
}

fun Player.playSound(sound : Sound, volume : Float = 1.0f, pitch : Float = 1.0f)
{
	playSound(location, sound, volume, pitch)
}