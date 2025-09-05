package com.aletropy.skya.island

import com.aletropy.skya.Skya
import com.aletropy.skya.campfire.CampfireManager
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.data.Island
import com.aletropy.skya.events.IslandCreatedEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Campfire
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Item
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

val CAMPFIRE_RELATIVE_LOCATION = Vector(2.0, 0.0, 1.0)
val ISLAND_RELATIVE_LOCATION = Vector(-7.0, -11.0, -7.0)
val SKY_CAMPFIRE_KEY = NamespacedKey(Skya.PLUGIN_ID, "sky_campfire")

class IslandManager(private val dbManager : DatabaseManager, val campfireManager: CampfireManager)
{
    private val islands = mutableMapOf<Location, Island>()

    private var currentRing = 0
    private var currentIndexInRing = 0

    companion object {
        private const val DEFAULT_Y = 100.0
        private const val RING_SPACING = 1000.0
    }

    fun loadAllIslands()
    {
        dbManager.getAllIslands().forEach { island ->
            islands[island.location] = island
        }
    }

    fun createIslandForGroup(groupId : Int, islandKey: NamespacedKey = Islands.DEFAULT_PLAYER_KEY) : Island
    {
        val location = getNearestValidLocation()

        generateNewIsland(location, islandKey)
        bindIslandToGroup(location, groupId)

        val campfire = campfireManager.getCampfire(location.clone().add(CAMPFIRE_RELATIVE_LOCATION))

        val island = Island(location, groupId, campfire)

        islands[location] = island
        IslandCreatedEvent(island).callEvent()

        return island
    }

    fun bindIslandToGroup(location: Location, groupId : Int) {
        val island = islands[location]

        if (island != null) return

        val campfireLocation = location.clone().add(CAMPFIRE_RELATIVE_LOCATION)

        if (campfireLocation.block.state is Campfire)
        {
            val state = campfireLocation.block.state as Campfire
            val pdc = state.persistentDataContainer
            pdc.set(SKY_CAMPFIRE_KEY, PersistentDataType.BOOLEAN, true)
            state.update()
        }

        campfireManager.bindCampfireToGroup(campfireLocation, groupId)
    }

    fun generateNewIsland(location : Location, islandKey : NamespacedKey)
    {
        if(!isValidLocation(location)) return

        val sm = Bukkit.getStructureManager()

        sm.loadStructure(NamespacedKey.minecraft("void"))?.place(
            location.clone().add(
                -24.0, -24.0, -24.0
            ), false, StructureRotation.NONE, Mirror.NONE, -1, 1.0f, Random()
        )

        val islandStructure = sm.loadStructure(islandKey) ?:
            throw IllegalArgumentException("$islandKey not exists in structure templates.")

        val baseLocation = location.clone()
        baseLocation.add(ISLAND_RELATIVE_LOCATION)

        islandStructure.place(
            baseLocation, true, StructureRotation.NONE, Mirror.NONE, -1, 1.0f, Random()
        )
    }

    fun deleteAllIslandsFromGroup(groupId: Int): Int
    {
        val groupIslands = dbManager.getGroupIslands(groupId)

        groupIslands.forEach {
            it.campfire?.let { campfire ->
                campfireManager.removeCampfire(campfire.location)
            }
            removeIslandInLocation(it.location)
            it.location.world.getNearbyEntitiesByType(
                Item::class.java, it.location, 48.0
            ).forEach { item -> item.remove() }
            islands.remove(it.location)
        }

        dbManager.removeGroupIslands(groupId)

        return groupIslands.size
    }

    fun removeIslandInLocation(location : Location)
    {
        if(isValidLocation(location)) return

        val sm = Bukkit.getStructureManager()
        val islandStructure = sm.loadStructure(NamespacedKey.minecraft("void")) ?:
        throw IllegalArgumentException("Island structure not exists.")

        val baseLocation = location.clone()
        baseLocation.add(-24.0, -24.0, -24.0)

        islandStructure.place(
            baseLocation, true, StructureRotation.NONE, Mirror.NONE, -1, 1.0f, Random()
        )

        recalculateIslandPositions()
    }

    private fun getNearestValidLocation() : Location
    {
        val world = Bukkit.getWorld("world") ?: throw IllegalAccessError("World 'world' does not exists!")

        while(true)
        {
            val location : Location? = when(currentRing) {
                0 -> {
                    if(currentIndexInRing == 0)
                    {
                        currentIndexInRing++
                        Location(world, 0.0, DEFAULT_Y, 0.0)
                    } else null
                }
                else -> {
                    val radius = currentRing * RING_SPACING

                    val numberOfIslandsInThisRing = 8 * currentRing
                    if(currentIndexInRing < numberOfIslandsInThisRing)
                    {
                        val angle = (2 * Math.PI / numberOfIslandsInThisRing) * currentIndexInRing

                        val x = radius * cos(angle)
                        val z = radius * sin(angle)

                        currentIndexInRing++
                        Location(world, x, DEFAULT_Y, z)
                    } else null
                }
            }

            if(location != null)
            {
                val roundedLocation = Location(world,
                    location.x.roundToInt().toDouble(),
                    location.y,
                    location.z.roundToInt().toDouble())

                if(isValidLocation(roundedLocation)) {
                    return roundedLocation
                } else {
                    currentRing++
                    currentIndexInRing = 0
                }
            } else {
                currentRing++
                currentIndexInRing = 0
            }
        }
    }

    private fun isValidLocation(location : Location) : Boolean
    {
        return islands[location] == null
    }

    private fun recalculateIslandPositions()
    {
        currentRing = 0
        currentIndexInRing = 0
    }

}