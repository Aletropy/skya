package com.aletropy.skya.api

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

interface CustomBlock
{
    val type : Material
    val stack : ItemStack

    fun getDataObject() : Any?
    fun setDataObject(data : Any)

    fun update(location : Location) { }

    fun onPlace(player : Player, block : Block) { }
    fun onBreak(block : Block, breaker : Player?) { }
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RegisterBlock(
    val id : String,
    val dataClass : KClass<*> = Nothing::class
)