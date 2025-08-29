package com.aletropy.skya.data

import com.aletropy.skya.events.BoundCampfireUpdateEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import java.io.File
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.logging.Level

class DatabaseManager(private val dataFolder : File)
{
    private var connection : Connection? = null

    init {
        connect()
        createTables()
    }

    private fun connect() {
        try {
            if (!dataFolder.exists())
                dataFolder.mkdirs()
            val dbFile = File(dataFolder, "skya.db")
            Class.forName("org.sqlite.JDBC")
            connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
        } catch (e : Exception) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not connect to SQLite database.")
        }
    }

    fun close() {
        try {
            connection?.close()
        } catch (e: Exception) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not close the SQLite connection!", e)
        }
    }

    private fun createTables()
    {
        val createGroupsTable = """
            CREATE TABLE IF NOT EXISTS groups (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                skyEssence INTEGER NOT NULL DEFAULT 0,
                islandLevel INTEGER NOT NULL DEFAULT 1
            );
        """
        val createMembersTable = """
            CREATE TABLE IF NOT EXISTS group_members (
                playerUUID TEXT PRIMARY KEY,
                groupId INTEGER NOT NULL,
                FOREIGN KEY(groupId) REFERENCES groups(id) ON DELETE CASCADE
            );
        """
        val createCampfiresTable = """
            CREATE TABLE IF NOT EXISTS bound_campfires (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                world TEXT NOT NULL,
                x INTEGER NOT NULL,
                y INTEGER NOT NULL,
                z INTEGER NOT NULL,
                groupId INTEGER NOT NULL,
                FOREIGN KEY(groupId) REFERENCES groups(id) ON DELETE CASCADE
            );
        """
        val createIslandsTable = """
            CREATE TABLE IF NOT EXISTS islands (
                world TEXT NOT NULL,
                x INTEGER NOT NULL,
                y INTEGER NOT NULL,
                z INTEGER NOT NULL,
                groupId INTEGER NOT NULL,
                campfireId INTEGER NOT NULL,
                PRIMARY KEY (world, x, y, z),
                FOREIGN KEY(campfireId) REFERENCES groups(id) ON DELETE CASCADE
                FOREIGN KEY(groupId) REFERENCES groups(id) ON DELETE CASCADE
            );
        """.trimIndent()
        connection?.createStatement()?.use { stmt ->
            stmt.execute(createGroupsTable)
            stmt.execute(createMembersTable)
            stmt.execute(createCampfiresTable)
            stmt.execute(createIslandsTable)
        }
    }

    fun createGroup(name: String): Int {
        val sql = "INSERT INTO groups (name, skyEssence, islandLevel) VALUES (?, 0, 0)"
        connection?.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)?.use { pstmt ->
            pstmt.setString(1, name)
            pstmt.executeUpdate()
            pstmt.generatedKeys?.use { keys ->
                if (keys.next()) {
                    return keys.getInt(1)
                }
            }
        }
        return -1
    }

    fun getGroupById(groupId: Int): Group? {
        val sql = "SELECT * FROM groups WHERE id = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setInt(1, groupId)
            pstmt.executeQuery()?.use { rs ->
                if (rs.next()) {
                    return rs.toGroup()
                }
            }
        }
        return null
    }

    fun getAllGroups() : List<Group> {
        val sql = "SELECT * FROM groups"
        val list = mutableListOf<Group>()
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.executeQuery()?.use { rs ->
                while(rs.next())
                    list.add(rs.toGroup())
            }
        }
        return list
    }

    fun updateGroup(group: Group) {
        val sql = "UPDATE groups SET name = ?, skyEssence = ?, islandLevel = ? WHERE id = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setString(1, group.name)
            pstmt.setInt(2, group.skyEssence)
            pstmt.setInt(3, group.islandLevel)
            pstmt.setInt(4, group.id)
            pstmt.executeUpdate()
        }
    }

    fun addPlayerToGroup(playerUUID: String, groupId: Int) {
        val sql = "INSERT OR REPLACE INTO group_members (playerUUID, groupId) VALUES (?, ?)"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setString(1, playerUUID)
            pstmt.setInt(2, groupId)
            pstmt.executeUpdate()
        }
    }

    fun getPlayerGroupId(playerUUID: String): Int? {
        val sql = "SELECT groupId FROM group_members WHERE playerUUID = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setString(1, playerUUID)
            pstmt.executeQuery()?.use { rs ->
                if (rs.next()) {
                    return rs.getInt("groupId")
                }
            }
        }
        return null
    }

    fun getPlayerGroup(playerUUID: String): Group? {
        val groupId = getPlayerGroupId(playerUUID) ?: return null
        return getGroupById(groupId)
    }

    fun getAllGroupMembers(groupId: Int) : List<String>
    {
        val uuids = mutableListOf<String>()
        val sql = "SELECT playerUUID FROM group_members WHERE groupId = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setInt(1, groupId)
            pstmt.executeQuery().use { rs ->
                while(rs.next())
                    uuids.add(rs.getString("playerUUID"))
            }
        }
        return uuids
    }

    fun storeBindedCampfire(location: Location, groupId: Int) {
        val sql = "INSERT INTO bound_campfires (world, x, y, z, groupId) VALUES (?, ?, ?, ?, ?)"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setString(1, location.world.name)
            pstmt.setInt(2, location.blockX)
            pstmt.setInt(3, location.blockY)
            pstmt.setInt(4, location.blockZ)
            pstmt.setInt(5, groupId)
            pstmt.executeUpdate()
        }
    }

    fun getBoundCampfire(location: Location): BoundCampfire? {
        val sql = "SELECT * FROM bound_campfires WHERE world = ? AND x = ? AND y = ? AND z = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setString(1, location.world.name)
            pstmt.setInt(2, location.blockX)
            pstmt.setInt(3, location.blockY)
            pstmt.setInt(4, location.blockZ)
            pstmt.executeQuery()?.use { rs ->
                if (rs.next()) {
                    return rs.toBoundCampfire()
                }
            }
        }
        return null
    }

    fun getCampfire(campfireId : Int): BoundCampfire? {
        val sql = "SELECT * FROM bound_campfires WHERE id = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setInt(1, campfireId)
            pstmt.executeQuery()?.use { rs ->
                if (rs.next()) {
                    return rs.toBoundCampfire()
                }
            }
        }
        return null
    }

    fun getGroupCampfires(groupId : Int) : List<BoundCampfire>
    {
        val list = mutableListOf<BoundCampfire>()
        val sql = "SELECT * FROM bound_campfires WHERE groupId = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setInt(1, groupId)
            pstmt.executeQuery()?.use { rs ->
                while(rs.next())
                    list.add(rs.toBoundCampfire())
            }
        }
        return list
    }

    fun removeBindedCampfire(location: Location) {
        val sql = "DELETE FROM bound_campfires WHERE world = ? AND x = ? AND y = ? AND z = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setString(1, location.world.name)
            pstmt.setInt(2, location.blockX)
            pstmt.setInt(3, location.blockY)
            pstmt.setInt(4, location.blockZ)
            pstmt.executeUpdate()
        }
    }

    fun getAllBoundCampfires(): List<BoundCampfire> {
        val campfires = mutableListOf<BoundCampfire>()
        val sql = "SELECT * FROM bound_campfires"
        connection?.createStatement()?.use { stmt ->
            stmt.executeQuery(sql)?.use { rs ->
                while (rs.next()) {
                    campfires.add(rs.toBoundCampfire())
                }
            }
        }
        return campfires
    }

    fun storeIsland(island: Island)
    {
        val sql = "INSERT INTO islands (world, x, y, z, groupId, campfireId) VALUES (?, ?, ?, ?, ?, ?)"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setString(1, island.location.world.name)
            pstmt.setInt(2, island.location.blockX)
            pstmt.setInt(3, island.location.blockY)
            pstmt.setInt(4, island.location.blockZ)
            pstmt.setInt(5, island.groupId)
            pstmt.setInt(6, island.campfire?.id ?: -1)
            pstmt.executeQuery()
        }
    }

    fun getGroupIslands(groupId : Int) : List<Island>
    {
        val list = mutableListOf<Island>()
        val sql = "SELECT * FROM islands WHERE groupId = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setInt(1, groupId)
            pstmt.executeQuery().use { rs ->
                while (rs.next())
                    list.add(rs.toIsland())
            }
        }
        return list
    }

    fun getAllIslands() : List<Island>
    {
        val list = mutableListOf<Island>()
        val sql = "SELECT * FROM islands"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.executeQuery().use { rs ->
                while (rs.next())
                    list.add(rs.toIsland())
            }
        }
        return list
    }

    private fun ResultSet.toGroup(): Group = Group(
        id = getInt("id"),
        name = getString("name"),
        skyEssence = getInt("skyEssence"),
        islandLevel = getInt("islandLevel")
    )

    private fun ResultSet.toBoundCampfire(): BoundCampfire {
        val world = Bukkit.getWorld(getString("world"))
            ?: throw IllegalStateException("World ${getString("world")} not found for bound campfire!")

        val location = Location(
            world,
            getInt("x").toDouble(),
            getInt("y").toDouble(),
            getInt("z").toDouble()
        )
        return BoundCampfire(getInt("id"), location, getInt("groupId"))
    }

    private fun ResultSet.toIsland(): Island {
        val world = Bukkit.getWorld(getString("world"))
            ?: throw IllegalStateException("World ${getString("world")} not found for island!")

        val location = Location(
            world,
            getInt("x").toDouble(),
            getInt("y").toDouble(),
            getInt("z").toDouble()
        )

        val campfire = getCampfire(getInt("campfireId"))

        return Island(location,getInt("groupId"), campfire)
    }

    fun addSkyEssenceToGroup(groupId: Int, amount : Int)
    {
        val group = getGroupById(groupId) ?: return
        group.skyEssence += amount
        updateGroup(group)
    }
}