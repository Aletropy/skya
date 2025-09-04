package com.aletropy.skya.blocks.custom

import com.aletropy.skya.Skya
import com.aletropy.skya.api.CustomBlock
import com.aletropy.skya.api.RegisterBlock
import com.aletropy.skya.blocks.createCustomBlockStack
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@RegisterBlock(
    "sky_essence_generator",
    SkyEssenceGenerator.Data::class
)
class SkyEssenceGenerator : CustomBlock
{
    @Serializable
    data class Data(val groupId : Int)
    private var data : Data? = null

    override val type = Material.BUDDING_AMETHYST
    override val stack = createCustomBlockStack(this, ItemStack(type)).let { stack ->
        stack.editMeta {
            it.setEnchantmentGlintOverride(true)
            it.displayName(
                Component.text("Sky Essence Generator MK1", NamedTextColor.LIGHT_PURPLE)
            )
        }
        stack
    }

    override fun onPlace(player: Player, block: Block)
    {
        Skya.INSTANCE.dbManager.getPlayerGroupId(player.uniqueId.toString())?.let {
            data = Data(it)
        }
    }

    override fun update(location: Location)
    {
        val groupId = data?.groupId ?: return
        val world = location.world
        Skya.INSTANCE.dbManager.addSkyEssenceToGroup(groupId, 1)

        world.spawnParticle(
            Particle.END_ROD, location.clone().add(0.5, 1.5, 0.5), 10, .1, .1, .1, 0.1
        )
    }

    override fun getDataObject(): Any? { return data }
    override fun setDataObject(data: Any) { if(data is Data) this.data = data }
}