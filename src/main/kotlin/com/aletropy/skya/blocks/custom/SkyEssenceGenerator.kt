package com.aletropy.skya.blocks.custom

import com.aletropy.skya.Skya
import com.aletropy.skya.blocks.CUSTOM_BLOCK_KEY
import com.aletropy.skya.blocks.CUSTOM_BLOCK_TYPE_KEY
import com.aletropy.skya.blocks.CustomBlock
import com.aletropy.skya.blocks.CustomBlocks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Marker
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

val GROUP_ID_KEY = NamespacedKey("skya", "group_id")

class SkyEssenceGenerator : CustomBlock
{
    override val type = Material.BUDDING_AMETHYST
    override val stack = run {
        val stack = ItemStack(type)
        stack.editPersistentDataContainer {
            it.set(CUSTOM_BLOCK_KEY, PersistentDataType.BOOLEAN, true)
            it.set(CUSTOM_BLOCK_TYPE_KEY, PersistentDataType.STRING, CustomBlocks.SKY_ESSENCE_GENERATOR)
        }
        stack.editMeta {
            it.setEnchantmentGlintOverride(true)
            it.displayName(
                Component.text("Sky Essence Generator MK1", NamedTextColor.LIGHT_PURPLE)
            )
        }
        stack
    }

    override fun onPlace(player: Player, block: Block, pdc: PersistentDataContainer)
    {
        val groupId = Skya.INSTANCE.dbManager.getPlayerGroupId(player.uniqueId.toString())!!
        pdc.set(GROUP_ID_KEY, PersistentDataType.INTEGER, groupId)
    }

    override fun update(location: Location, pdc: PersistentDataContainer)
    {
        val world = location.world
        val groupId = pdc.get(GROUP_ID_KEY, PersistentDataType.INTEGER) ?: return
        Skya.INSTANCE.dbManager.addSkyEssenceToGroup(groupId, 1)

        world.spawnParticle(
            Particle.END_ROD, location.clone().add(0.5, 1.5, 0.5), 10
        )
    }

    override fun onBreak(block: Block, entity: Marker, destroyer: Player?)
    {
        if(destroyer == null) return

        destroyer.sendMessage("You destroyed me :(")
    }
}