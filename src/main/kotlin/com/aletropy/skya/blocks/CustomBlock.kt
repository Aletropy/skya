package com.aletropy.skya.blocks

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Marker
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer


interface CustomBlock
{
    val type : Material
    val stack : ItemStack

    fun update(location : Location, pdc : PersistentDataContainer) { }

    fun onPlace(player : Player, block : Block, pdc : PersistentDataContainer) { }
    fun onBreak(block : Block, entity : Marker, destroyer : Player?) { }
}