package com.aletropy.skya.menus

enum class Arrangement
{
	TOP_LEFT, TOP_CENTER, TOP_RIGHT,
	CENTER_LEFT, CENTER, CENTER_RIGHT,
	BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;

	fun getSlot(size : Int) : Int {
		val rows = size / 9
		val middleRow = (rows-1) / 2
		val middleCol = 4

		return when(this) {
			TOP_LEFT -> 0
			TOP_CENTER -> middleCol
			TOP_RIGHT -> 8
			CENTER_LEFT -> middleRow * 9
			CENTER -> middleRow * 9 + middleCol
			CENTER_RIGHT -> middleRow * 9 + 8
			BOTTOM_LEFT -> (rows - 1) * 9
			BOTTOM_CENTER -> (rows - 1) * 9 + middleCol
			BOTTOM_RIGHT -> size - 1
		}
	}
}