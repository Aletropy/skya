package com.aletropy.skya.listeners

import com.aletropy.skya.Skya
import com.aletropy.skya.blocks.CUSTOM_BLOCK_KEY
import com.aletropy.skya.blocks.CUSTOM_BLOCK_TYPE_KEY
import com.aletropy.skya.blocks.custom.CustomBlocks
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.persistence.PersistentDataType

@RegisterListener
class BlocksListener : Listener
{
    private val blockManager = Skya.INSTANCE.blockManager

    @EventHandler
    fun onChunkLoaded(event : ChunkLoadEvent)
    {
        blockManager.loadBlocksForChunk(event.chunk)
    }

    @EventHandler
    fun onChunkUnloaded(event : ChunkUnloadEvent)
    {
        blockManager.unloadBlocksForChunk(event.chunk)
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

        val cbInstance = cbClass.constructors.first().call()

        blockManager.placeBlock(location, cbInstance, player)
    }

    @EventHandler
    fun onBlockBroken(event : BlockBreakEvent)
    {
        val location = event.block.location.toBlockLocation()
        val customBlock = blockManager.blocks[location] ?: return

        val customStack = customBlock.stack
        event.isDropItems = false
        location.world.dropItemNaturally(location.clone().add(0.5, 0.5, 0.5), customStack)

        customBlock.onBreak(event.block, event.player)
        blockManager.removeBlock(location, event.player)
    }
}