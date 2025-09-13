package com.aletropy.skya.api.util

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class ItemBuilder(type : Material, amount : Int = 1)
{
	private var stack = ItemStack(type, amount)

	constructor(stack : ItemStack) : this(stack.type, stack.amount)
	{
		this.stack = stack
	}

	fun name(name : Component) : ItemBuilder
	{
		stack.editMeta { it.displayName(name) }
		return this
	}

	fun description(vararg lines : Component) : ItemBuilder
	{
		return description(lines.toList())
	}

	fun description(lines : List<Component>) : ItemBuilder
	{
		stack.editMeta {
			it.lore(lines)
		}
		return this
	}

	fun hideTooltip(hide : Boolean) : ItemBuilder
	{
		stack.editMeta {
			it.isHideTooltip = true
		}
		return this
	}

	fun amount(amount : Int) : ItemBuilder
	{
		stack.amount = amount
		return this
	}

	fun enchantmentGlint(active : Boolean) : ItemBuilder
	{
		stack.editMeta { it.setEnchantmentGlintOverride(active) }
		return this
	}

	fun build() = stack
}