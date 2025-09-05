package com.aletropy.skya.blocks

import com.aletropy.skya.Skya
import com.aletropy.skya.api.CustomBlock
import com.aletropy.skya.api.ITickable
import com.aletropy.skya.blocks.custom.CustomBlocks
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

val CUSTOM_BLOCK_KEY = NamespacedKey(Skya.PLUGIN_ID, "custom_block")
val CUSTOM_BLOCK_TYPE_KEY = NamespacedKey(Skya.PLUGIN_ID, "custom_block_type")

fun createCustomBlockStack(customBlock : CustomBlock, stack : ItemStack) : ItemStack
{
    return stack.let { itemStack ->
        itemStack.editMeta {
            it.persistentDataContainer.set(CUSTOM_BLOCK_KEY, PersistentDataType.BOOLEAN, true)
            it.persistentDataContainer.set(CUSTOM_BLOCK_TYPE_KEY, PersistentDataType.STRING,
                CustomBlocks.NAMES[customBlock::class]!!)
        }
        itemStack
    }
}

class BlockManager(plugin : Skya)
{
    val blocks = mutableMapOf<Location, CustomBlock>()
    private val dbManager = plugin.dbManager

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    init {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            blocks.toMap().forEach { (location, customBlock) ->
                if(customBlock is ITickable)
                    customBlock.update(location)
            }
        }, 20L, 20L)
    }

    fun loadBlocksForChunk(chunk : Chunk)
    {
        val chunkBlocks = dbManager.getBlocksInChunk(chunk.world.name, chunk.x, chunk.z)

        chunkBlocks.forEach { (location, pair) ->
            val (blockType, data) = pair
            val cbClass = CustomBlocks.BLOCKS[blockType] ?: return@forEach
            val cbInstance = cbClass.constructors.first().call()

            if(data.isNotBlank() && data != "{}") {
                CustomBlocks.SERIALIZERS[blockType]?.let { serializer ->
                    val dataObject = json.decodeFromString(serializer, data)
                    cbInstance.setDataObject(dataObject)
                }
            }

            cbInstance.onLoad(location.toBlockLocation().block)
            blocks[location.toBlockLocation()] = cbInstance
        }
    }

    fun unloadBlocksForChunk(chunk: Chunk) {
        val min = Vector(chunk.x, 0, chunk.z)
        val max = Vector(chunk.x+16, 0, chunk.z+16)

        blocks.keys.removeIf { loc ->
            loc.world == chunk.world &&
                    loc.blockX >= min.x && loc.blockX <= max.x &&
                    loc.blockZ >= min.z && loc.blockZ <= max.z
        }
    }

    fun placeBlock(location: Location, cb: CustomBlock, player : Player) {
        val blockType = CustomBlocks.NAMES[cb::class] ?: return

        location.block.type = cb.type
        cb.onPlace(player, location.block, player.inventory.itemInMainHand)
        cb.onLoad(location.block)
        blocks[location] = cb

        val dataObject = cb.getDataObject()
        val dataString = if(dataObject != null) {
            CustomBlocks.SERIALIZERS[blockType]?.let {
                @Suppress("UNCHECKED_CAST")
                json.encodeToString(it as KSerializer<Any>, dataObject)
            } ?: "{}"
        } else "{}"

        dbManager.storeCustomBlock(location, blockType, dataString)
    }

    fun saveBlockState(location: Location, customBlock: CustomBlock) {
        val blockType = CustomBlocks.NAMES[customBlock::class] ?: return
        val dataObject = customBlock.getDataObject()

        val dataString = if (dataObject != null) {
            CustomBlocks.SERIALIZERS[blockType]?.let { serializer ->
                @Suppress("UNCHECKED_CAST")
                json.encodeToString(serializer as KSerializer<Any>, dataObject)
            } ?: "{}"
        } else "{}"

        dbManager.storeCustomBlock(location, blockType, dataString)
    }

    fun removeBlock(location: Location, player : Player?)
    {
        val customBlock = blocks[location] ?: return

        customBlock.onBreak(location.block, player)

        location.block.type = Material.AIR
        blocks.remove(location)
        dbManager.removeCustomBlock(location)
    }
}