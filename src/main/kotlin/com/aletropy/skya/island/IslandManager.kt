package com.aletropy.skya.island

import com.aletropy.skya.campfire.CampfireManager
import com.aletropy.skya.data.DatabaseManager
import com.aletropy.skya.data.Island
import com.aletropy.skya.events.IslandCreatedEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.util.Vector
import java.util.Random
import javax.xml.crypto.Data
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

val CAMPFIRE_RELATIVE_LOCATION = Vector(2.0, 0.0, 1.0)
val ISLAND_RELATIVE_LOCATION = Vector(-7.0, -11.0, -7.0)

class IslandManager(private val dbManager : DatabaseManager, val campfireManager: CampfireManager)
{
    private val islands = mutableMapOf<Location, Island>()

    private var currentRing = 0
    private var currentIndexInRing = 0

    companion object {
        private const val DEFAULT_Y = 100.0
        private const val RING_SPACING = 1000.0
    }

    fun createIslandForGroup(groupId : Int)
    {
        val location = getNearestValidLocation()

        generateNewIsland(location)
        bindIslandToGroup(location, groupId)

        val campfire = campfireManager.getCampfire(location.clone().add(CAMPFIRE_RELATIVE_LOCATION))

        val island = Island(location, groupId, campfire)

        islands[location] = island

        IslandCreatedEvent(island).callEvent()
    }

    fun bindIslandToGroup(location: Location, groupId : Int)
    {
        val island = islands[location]

        if(island != null) return

        val campfireLocation = location.clone().add(CAMPFIRE_RELATIVE_LOCATION)
        campfireManager.bindCampfireToGroup(campfireLocation, groupId)
    }

    fun generateNewIsland(location : Location)
    {
        if(!isValidLocation(location)) return

        val sm = Bukkit.getStructureManager()
        val islandStructure = sm.loadStructure(NamespacedKey.minecraft("island")) ?:
            throw IllegalArgumentException("Island structure not exists.")

        val baseLocation = location.clone()
        baseLocation.add(ISLAND_RELATIVE_LOCATION)

        islandStructure.place(
            baseLocation, true, StructureRotation.NONE, Mirror.NONE, -1, 1.0f, Random()
        )
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

    fun loadAllIslands()
    {
        dbManager.getAllIslands().forEach { island ->
            islands[island.location] = island
        }
    }

    private fun isValidLocation(location : Location) : Boolean
    {
        return islands[location] == null
    }

}