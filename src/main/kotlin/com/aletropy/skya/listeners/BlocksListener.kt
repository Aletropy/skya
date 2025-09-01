package com.aletropy.skya.listeners

import com.aletropy.skya.blocks.BlockManager
import com.aletropy.skya.blocks.CUSTOM_BLOCK_KEY
import com.aletropy.skya.blocks.CUSTOM_BLOCK_TYPE_KEY
import com.aletropy.skya.blocks.CustomBlocks
import org.bukkit.entity.Marker
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType

class BlocksListener(private val blockManager: BlockManager) : Listener
{
    @EventHandler
    fun onChunkLoaded(event : ChunkLoadEvent)
    {
        val chunk = event.chunk

        chunk.entities.forEach {
            if(it !is Marker) return@forEach
            if(blockManager.blocks[it.location.toBlockLocation()] != null) return@forEach

            blockManager.loadCustomBlock(it.location.toBlockLocation(), it)
        }
    }

    @EventHandler
    fun onBlockPlaced(event : BlockPlaceEvent)
    {
        val player = event.player
        val stack = event.itemInHand
        val location = event.blockPlaced.location.toBlockLocation()

        if(!stack.persistentDataContainer.has(CUSTOM_BLOCK_KEY)) return

        val cbClass = CustomBlocks.BLOCKS[stack.persistentDataContainer.get(
            CUSTOM_BLOCK_TYPE_KEY, PersistentDataType.STRING
        )] ?: return

        val cb = cbClass.constructors.first().call()

        val instance = blockManager.createBlock(
            location, cb
        )

        cb.onPlace(player, event.blockPlaced, instance.entity.persistentDataContainer)
    }

    @EventHandler
    fun onBlockBroken(event : BlockBreakEvent)
    {
        val block = event.block
        val location = block.location
        val world = block.world

        val entity = world.getNearbyEntitiesByType(
            Marker::class.java, location, 0.5
        ).firstOrNull() ?: return

        if(!entity.persistentDataContainer.has(CUSTOM_BLOCK_KEY)) return

        blockManager.blocks[location.toBlockLocation()]?.onBreak(
            block, entity, event.player
        )

        blockManager.removeBlock(location)
    }
}