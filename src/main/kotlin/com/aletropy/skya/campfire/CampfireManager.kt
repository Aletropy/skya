package com.aletropy.skya.campfire

import com.aletropy.skya.data.BoundCampfire
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.events.BoundCampfireRemovedEvent
import com.aletropy.skya.events.BoundedCampfireEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay

class CampfireManager(private val dbManager : DatabaseManager)
{
    private val activeDisplays = mutableMapOf<Location, List<TextDisplay>>()

    fun bindCampfireToGroup(location : Location, groupId : Int)
    {
        val campfire = BoundCampfire(location, groupId)
        BoundedCampfireEvent(campfire).callEvent()
        updateCampfireDisplay(location, groupId)
    }

    fun getCampfire(location : Location) : BoundCampfire?
    {
        return dbManager.getBoundCampfire(location)
    }

    fun updateCampfireDisplay(location : Location, groupId : Int)
    {
        if(location.block.type != Material.CAMPFIRE) return

        activeDisplays.remove(location)?.forEach { it.remove() }
        val oldEntitiesLocation = location.clone().add(0.5, 1.5, 0.5)
        location.world.getNearbyEntitiesByType(TextDisplay::class.java, oldEntitiesLocation, 1.5).forEach { it.remove() }

        val group = dbManager.getGroupById(groupId) ?: return
        val displayLocation = location.clone().add(0.5, 1.5, 0.5)

        val levelDisplay = location.world.spawnEntity(displayLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        levelDisplay.text(Component.text(" - Level: ", NamedTextColor.AQUA)
            .append(Component.text("${group.islandLevel}", NamedTextColor.GRAY)))
        levelDisplay.billboard = Display.Billboard.CENTER

        displayLocation.add(0.0, 0.3, 0.0)
        val essenceDisplay = location.world.spawnEntity(displayLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        essenceDisplay.text(Component.text(" - Sky Essence: ", NamedTextColor.AQUA)
            .append(Component.text("${group.skyEssence} SE", NamedTextColor.GREEN)))
        essenceDisplay.billboard = Display.Billboard.CENTER

        displayLocation.add(0.0, 0.3, 0.0)
        val nameDisplay = location.world.spawnEntity(displayLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        nameDisplay.text(Component.text("- ${group.name}'s Campfire -", NamedTextColor.GOLD))
        nameDisplay.billboard = Display.Billboard.CENTER

        activeDisplays[location] = listOf(levelDisplay, essenceDisplay, nameDisplay)
    }

    fun removeCampfire(location : Location)
    {
        activeDisplays.remove(location)?.forEach { it.remove() }
        BoundCampfireRemovedEvent(location).callEvent()
    }

    fun loadAllCampfires() {
        dbManager.getAllBoundCampfires().forEach { campfire ->
            updateCampfireDisplay(campfire.location, campfire.groupId)
        }
    }
}