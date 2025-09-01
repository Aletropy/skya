package com.aletropy.skya.blocks

import com.aletropy.skya.Skya
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Marker
import org.bukkit.persistence.PersistentDataType

val CUSTOM_BLOCK_KEY = NamespacedKey(Skya.PLUGIN_ID, "custom_block")
val CUSTOM_BLOCK_TYPE_KEY = NamespacedKey(Skya.PLUGIN_ID, "custom_block_type")

data class CustomBlockInstance(val customBlock: CustomBlock, val entity: Marker)

class BlockManager(plugin : Skya)
{
    val blocks = mutableMapOf<Location, CustomBlock>()

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            blocks.forEach {
                val location = it.key
                val cb = it.value

                val world = location.world

                val blockEntity = world.getNearbyEntitiesByType(
                        Marker::class.java, location, 0.5
                    ).firstOrNull()

                if(blockEntity == null) {
                    removeBlock(location)
                    return@forEach
                }

                cb.update(location, blockEntity.persistentDataContainer)
            }
        }, 0L, 20L)
    }

    fun loadAllCustomBlocks()
    {
        Bukkit.getWorlds().forEach { world ->
            val markers = world.getEntitiesByClass(Marker::class.java)
            for(marker in markers)
                loadCustomBlock(marker.location.toBlockLocation(), marker)
        }
    }

    fun loadCustomBlock(location : Location, entity : Marker)
    {
        if(!entity.persistentDataContainer.has(CUSTOM_BLOCK_KEY)) return

        val type = entity.persistentDataContainer.getOrDefault(
            CUSTOM_BLOCK_TYPE_KEY, PersistentDataType.STRING, String()
        )

        val customBlockClazz = CustomBlocks.BLOCKS[type] ?: return
        val customBlock = customBlockClazz.constructors.first().call()

        blocks[location] = customBlock
    }

    fun createBlock(location : Location, cb : CustomBlock) : CustomBlockInstance
    {
        location.block.type = cb.type
        val world = location.world

        val entity = world.createEntity(location, Marker::class.java)
        entity.persistentDataContainer.set(
            CUSTOM_BLOCK_KEY, PersistentDataType.BOOLEAN, true
        )
        entity.persistentDataContainer.set(
            CUSTOM_BLOCK_TYPE_KEY, PersistentDataType.STRING, CustomBlocks.NAMES[cb::class]!!
        )

        world.addEntity(entity)

        blocks[location] = cb

        return CustomBlockInstance(cb, entity)
    }

    fun removeBlock(location : Location)
    {
        location.block.type = Material.AIR

        location.world.getNearbyEntitiesByType(
            Marker::class.java, location, 0.1
        ).forEach { it.remove() }
    }
}