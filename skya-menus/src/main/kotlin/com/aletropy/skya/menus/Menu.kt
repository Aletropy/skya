package com.aletropy.skya.menus

import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

object Rows {
	const val ONE = 9
	const val TWO = 18
	const val THREE = 27
	const val FOUR = 36
	const val FIVE = 45
	const val SIX = 54
}

data class ActionContext(val player : Player, val clickType: ClickType)

@Suppress("unused")
abstract class Menu(
	val plugin : JavaPlugin,
	val player: Player,
	var currentPage : Int = 0
) : InventoryHolder
{
	lateinit var title: Component
	var size: Int = 27

	private val buttons = mutableMapOf<Int, (ActionContext) -> Unit>()
	private val items = mutableMapOf<Int, ItemStack>()

	private var inventory: Inventory? = null

	fun title(component: Component)
	{
		title = component
	}

	fun size(rows: Int)
	{
		this.size = rows
	}

	fun slot(slot: Int, item: ItemStack, onClick: ((ActionContext) -> Unit)? = null)
	{
		items[slot] = item
		if (onClick != null) buttons[slot] = onClick
	}

	fun button(arrangement: Arrangement, item: ItemStack, onClick: ((ActionContext) -> Unit)? = null)
	{
		slot(arrangement.getSlot(size), item, onClick)
	}

	fun fill(item: ItemStack, range: IntRange = 0 until size)
	{
		for (i in range)
		{
			if (!items.containsKey(i)) slot(i, item)
		}
	}

	fun <T> paginated(
		items: List<T>,
		itemSlots: List<Int>,
		itemBuilder: (T) -> ItemStack,
		onClick: (ActionContext, T) -> Unit
	)
	{
		val startIndex = currentPage * itemSlots.size
		val endIndex = minOf(startIndex + itemSlots.size, items.size)

		if (startIndex >= items.size) return

		val pageItems = items.subList(startIndex, endIndex)
		for ((index, item) in pageItems.withIndex())
		{
			val slot = itemSlots.getOrNull(index) ?: break
			slot(slot, itemBuilder(item)) { context ->
				onClick(context, item)
			}
		}
	}

	fun nextPageButton(slot: Int, item: ItemStack)
	{
		slot(slot, item) {
			it.player.playSound(it.player.location, Sound.UI_BUTTON_CLICK, 1f, 1.2f)
			this.currentPage++
			this.open() // Reabre o menu na nova página
		}
	}

	fun previousPageButton(slot: Int, item: ItemStack)
	{
		if (currentPage == 0) return
		slot(slot, item) {
			it.player.playSound(it.player.location, Sound.UI_BUTTON_CLICK, 1f, 1.2f)
			this.currentPage--
			this.open()
		}
	}

	protected abstract fun build()

	fun open() {
		this.items.clear()
		this.buttons.clear()

		build()

		val inv = plugin.server.createInventory(this, size, title)
		items.forEach { (slot, item) -> inv.setItem(slot, item) }
		this.inventory = inv
		player.openInventory(inv)
	}

	internal fun handleClick(slot: Int, clickType: ClickType) {
		buttons[slot]?.invoke(ActionContext(player, clickType))
	}

	override fun getInventory(): Inventory = inventory ?: throw IllegalStateException("Inventory not created yet!")
}