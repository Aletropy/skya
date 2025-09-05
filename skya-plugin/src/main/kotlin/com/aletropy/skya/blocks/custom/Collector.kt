package com.aletropy.skya.blocks.custom

import com.aletropy.skya.Skya
import com.aletropy.skya.api.CustomBlock
import com.aletropy.skya.api.ITickable
import com.aletropy.skya.api.RegisterBlock
import com.aletropy.skya.blocks.createCustomBlockStack
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

@RegisterBlock(
    "collector",
    Collector.Data::class
)
class Collector : CustomBlock, ITickable
{
    @Serializable
    data class Data(var level : Int)
    private var data : Data? = null

    override val type = Material.CONDUIT
    override val stack = createCustomBlockStack(this, ItemStack(type)).let { item ->
        item.editMeta {
            it.displayName(Component.text("Collector", NamedTextColor.GOLD))
            it.lore(listOf(Component.space(), Component.text("Collects all items in a 5x5x5 area")))
            it.setEnchantmentGlintOverride(true)
        }
        item
    }

    override fun update(location: Location)
    {
        Bukkit.getScheduler().runTask(Skya.INSTANCE, Runnable {
            val items = location.world.getNearbyEntitiesByType(Item::class.java, location, 5.0)
            val stacks = items.map { item -> item.itemStack }

            val bottom = location.block.getRelative(0, -1, 0)
            val chest : Chest? = if(bottom.type == Material.CHEST) bottom.state as Chest else null

            if(chest == null) {
                items.forEach {
                    it.teleport(location.clone().add(0.5, 1.5, 0.5))
                    location.world.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f)
                    location.world.spawnParticle(
                        Particle.ASH, location.clone().add(0.5, 1.5, 0.5),
                        10, .1, .1, .1, .1
                    )
                }
                return@Runnable
            }
            val chestInv = chest.blockInventory

            if(stacks.isEmpty()) return@Runnable

            location.world.playSound(location, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f)
            location.world.spawnParticle(
                Particle.HAPPY_VILLAGER, location.clone().add(0.5, 0.5, 0.5),
                10, .1, .1, .1, .1
            )
            chestInv.addItem(*stacks.toTypedArray())
            items.forEach { it.remove() }
        })
    }

    override fun getDataObject(): Any? { return data }

    override fun setDataObject(data: Any) { if(data is Data) this.data = data }
}