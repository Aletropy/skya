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

    fun onLoad(block : Block) { }
    fun onPlace(player : Player, block : Block, blockStack : ItemStack) { }
    fun onBreak(block : Block, breaker : Player?) { }
}

interface ITickable
{
    fun update(location : Location)
}

interface IInteractable {
    fun onInteract(player : Player, block: Block)
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RegisterBlock(
    val id : String,
    val dataClass : KClass<*> = Nothing::class
)