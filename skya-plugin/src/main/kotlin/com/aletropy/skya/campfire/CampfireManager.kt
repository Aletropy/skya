package com.aletropy.skya.campfire

import com.aletropy.skya.Skya
import com.aletropy.skya.data.BoundCampfire
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.economy.PassiveIncomeSource
import com.aletropy.skya.events.BoundCampfireRemovedEvent
import com.aletropy.skya.events.BoundedCampfireEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class CampfireDisplayData(
    val displays: List<TextDisplay>,
    var displayedEssence: Double,
    var incomePerSecond: Int,
    var groupName: String,
    var groupColor: Int,
    var islandLevel: Int
)

class CampfireManager(private val plugin: Skya, private val dbManager: DatabaseManager) {
    private val activeCampfires = ConcurrentHashMap<Location, CampfireDisplayData>()
    private val numberFormatter: NumberFormat = NumberFormat.getInstance(Locale.US)

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            activeCampfires.forEach { (_, data) ->
                data.displayedEssence += (data.incomePerSecond / 20.0)
                updateTextDisplays(data)
            }
        }, 0L, 1L)

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            val groupDataCache = mutableMapOf<Int, Pair<Int, Int>>()

            dbManager.getAllBoundCampfires().forEach { campfire ->
                val groupId = campfire.groupId
                val (essence, income) = groupDataCache.getOrPut(groupId) {
                    val currentIncome = plugin.skyEssenceManager.getPassiveIncomeForGroup(groupId)
                    val currentEssence = plugin.skyEssenceManager.getEssence(groupId)
                    Pair(currentEssence, currentIncome)
                }

                activeCampfires[campfire.location]?.let {
                    it.displayedEssence = essence.toDouble()
                    it.incomePerSecond = income

                    dbManager.getGroupById(groupId)?.also { group ->
                        it.groupName = group.name
                        it.groupColor = group.color
                        it.islandLevel = group.islandLevel
                    }
                }
            }
        }, 0L, 20L)
    }

    private fun updateTextDisplays(data: CampfireDisplayData) {
        if (data.displays.size < 3) return

        val levelDisplay = data.displays[0]
        levelDisplay.text(Component.text(" - Level: ", NamedTextColor.AQUA)
            .append(Component.text("${data.islandLevel}", NamedTextColor.GRAY)))

        val essenceDisplay = data.displays[1]
        val formattedEssence = numberFormatter.format(data.displayedEssence.toLong())
        essenceDisplay.text(Component.text(" - Sky Essence: ", NamedTextColor.AQUA)
            .append(Component.text("$formattedEssence SE", NamedTextColor.GREEN)))

        val nameDisplay = data.displays[2]
        nameDisplay.text(Component.text("- ${data.groupName}'s Campfire -", NamedTextColor.namedColor(data.groupColor)))
    }

    private fun createTextDisplays(location: Location, groupId: Int) {
        val group = dbManager.getGroupById(groupId) ?: return
        val displayLocation = location.clone().add(0.5, 1.5, 0.5)

        val textEntities = location.world.getNearbyEntitiesByType(TextDisplay::class.java, displayLocation, 1.5)
        for(entity in textEntities)
        {
            entity.remove()
        }

        val levelDisplay = createDisplay(displayLocation, "")
        displayLocation.add(0.0, 0.3, 0.0)
        val essenceDisplay = createDisplay(displayLocation, "")
        displayLocation.add(0.0, 0.3, 0.0)
        val nameDisplay = createDisplay(displayLocation, "")

        val displays = listOf(levelDisplay, essenceDisplay, nameDisplay)
        val displayData = CampfireDisplayData(
            displays = displays,
            displayedEssence = group.skyEssence.toDouble(),
            incomePerSecond = 0,
            groupName = group.name,
            groupColor = group.color,
            islandLevel = group.islandLevel
        )

        activeCampfires[location] = displayData
        updateTextDisplays(displayData)
    }

    private fun createDisplay(location: Location, text: String): TextDisplay {
        return (location.world.spawnEntity(location, EntityType.TEXT_DISPLAY) as TextDisplay).apply {
            this.text(Component.text(text))
            this.billboard = Display.Billboard.CENTER
        }
    }

    fun bindCampfireToGroup(location: Location, groupId: Int) {
        BoundedCampfireEvent(BoundCampfire(0, location, groupId)).callEvent()
        createTextDisplays(location, groupId)
        plugin.skyEssenceManager.registerPassiveSource(groupId, PassiveIncomeSource(location, 1))
    }

    fun removeCampfire(location: Location) {
        dbManager.getBoundCampfire(location)?.let { campfire ->
            plugin.skyEssenceManager.unregisterPassiveSource(campfire.groupId, location)
        }

        activeCampfires.remove(location)?.displays?.forEach { it.remove() }
        BoundCampfireRemovedEvent(location).callEvent()
    }

    fun getCampfire(location: Location) : BoundCampfire?
    {
        return dbManager.getBoundCampfire(location)
    }

    fun loadAllCampfires() {
        dbManager.getAllBoundCampfires().forEach { campfire ->
            createTextDisplays(campfire.location, campfire.groupId)
            plugin.skyEssenceManager.registerPassiveSource(campfire.groupId, PassiveIncomeSource(campfire.location, 1))
        }
    }
}