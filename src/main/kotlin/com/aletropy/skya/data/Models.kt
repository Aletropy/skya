package com.aletropy.skya.data

import org.bukkit.Location
import java.util.UUID

data class Group(val id : Int, var name : String, var skyEssence : Int, var islandLevel : Int)

data class GroupInvite(val inviterUUID : UUID, val groupId : Int, val inviterGroupName : String, val timestamp : Long =
    System.currentTimeMillis())

data class GroupMember(val playerUUID : String, val groupId : Int)
data class BoundCampfire(val location : Location, val groupId : Int)