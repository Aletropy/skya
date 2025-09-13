package com.aletropy.skya.blocks.custom

import BalanceConfig
import com.aletropy.skya.Skya
import com.aletropy.skya.api.CustomBlock
import com.aletropy.skya.api.IInteractable
import com.aletropy.skya.api.ITickable
import com.aletropy.skya.api.RegisterBlock
import com.aletropy.skya.blocks.createCustomBlockStack
import com.aletropy.skya.economy.PassiveIncomeSource
import com.aletropy.skya.extensions.getGroupId
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@RegisterBlock(
    "sky_essence_generator",
    SkyEssenceGenerator.Data::class
)
class SkyEssenceGenerator : CustomBlock, ITickable, IInteractable
{
    @Serializable
    data class Data(val groupId : Int, var level : Int)
    private var data : Data? = null

	companion object {
		val STACK = createCustomBlockStack(SkyEssenceGenerator::class, ItemStack(Material.BUDDING_AMETHYST)).let { stack ->
			stack.editMeta {
				it.setEnchantmentGlintOverride(true)
				it.displayName(
					Component.text("Sky Essence Generator", NamedTextColor.LIGHT_PURPLE)
				)
				it.lore(listOf(
					Component.text("Level: 1", NamedTextColor.GRAY)
				))
			}
			stack
		}
	}

    override val type = Material.BUDDING_AMETHYST
    override val stack = STACK

    override fun onLoad(block: Block)
    {
        if(data == null) return
        Skya.INSTANCE.skyEssenceManager.registerPassiveSource(data!!.groupId,
            PassiveIncomeSource(block.location.toBlockLocation(),
				BalanceConfig.getGeneratorProduction(data!!.level)))
    }

    override fun onPlace(player: Player, block: Block, blockStack: ItemStack)
    {
        Skya.INSTANCE.dbManager.getPlayerGroupId(player.uniqueId.toString())?.let {
            data = Data(it, 1)
        }
    }

    override fun onBreak(block: Block, breaker: Player?)
    {
        Skya.INSTANCE.skyEssenceManager.unregisterPassiveSource(
            data!!.groupId, block.location.toBlockLocation()
        )
    }

    override fun onInteract(player: Player, block: Block)
    {
        if(data?.groupId != player.getGroupId()) {
            player.sendMessage(Component.text("This generator isn't yours.", NamedTextColor.RED))
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f)
            return
        }

        val currentLevel = data?.level ?: 1
        val currentProd = BalanceConfig.getGeneratorProduction(currentLevel)
        val nextCost = BalanceConfig.getGeneratorUpgradeCost(currentLevel)
        val command = "/gen upgrade ${block.world.name} ${block.x} ${block.y} ${block.z}"

        sendGeneratorInfo(player, currentLevel, nextCost.toInt(), currentProd, command)
    }

    private fun sendGeneratorInfo(player : Player, currentLevel : Int, nextCost : Int, currentProd : Int, command :
    String)
    {
        player.sendMessage(Component.text("--- SE Generator ---", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
        player.sendMessage(Component.text(" Level: ", NamedTextColor.GRAY).append(Component.text(currentLevel,
            NamedTextColor.WHITE)))
        player.sendMessage(Component.text(" Production: ", NamedTextColor.GRAY).append(Component.text("$currentProd " +
                "SE/s", NamedTextColor.AQUA)))
        player.sendMessage(Component.text(" Level Cost ${currentLevel + 1}: ", NamedTextColor.GRAY).append(Component
            .text("$nextCost SE", NamedTextColor.GOLD)))
        player.sendMessage(
            Component.text("[UPGRADE]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Click to evolve!")))
                .clickEvent(ClickEvent.runCommand(command)))
    }

    override fun update(location: Location)
    {
        val world = location.world

        val level = data?.level ?: 1
        val count = Math.clamp(level.toLong(), 0, 100)

        world.spawnParticle(
            Particle.END_ROD, location.clone().add(0.5, 1.5, 0.5), count, .1, .1, .1, 0.05
        )
    }

    override fun getDataObject(): Any? { return data }
    override fun setDataObject(data: Any) { if(data is Data) this.data = data }
}