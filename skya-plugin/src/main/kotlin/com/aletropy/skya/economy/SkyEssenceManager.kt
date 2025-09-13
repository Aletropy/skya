package com.aletropy.skya.economy

import com.aletropy.skya.Skya
import com.aletropy.skya.data.TransactionReason
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.concurrent.ConcurrentHashMap


class SkyEssenceManager(private val plugin: Skya)
{
	private val dbManager = plugin.dbManager
	private val sourcesByGroup = ConcurrentHashMap<Int, ConcurrentHashMap<Location, PassiveIncomeSource>>()

	init
	{
		startPassiveIncomeLoop()
	}

	fun registerPassiveSource(groupId: Int, source: PassiveIncomeSource)
	{
		sourcesByGroup.computeIfAbsent(groupId) {
			ConcurrentHashMap()
		}
		sourcesByGroup[groupId]?.put(source.location, source)
	}

	fun unregisterPassiveSource(groupId: Int, location: Location)
	{
		sourcesByGroup[groupId]?.remove(location)
		if (sourcesByGroup[groupId]?.isEmpty() == true)
		{
			sourcesByGroup.remove(groupId)
		}
	}

	fun addEssence(groupId: Int, amount: Int, reason: TransactionReason)
	{
		if (amount <= 0) return
		dbManager.addSkyEssenceToGroup(groupId, amount)
	}

	fun removeEssence(groupId: Int, amount: Int, reason: TransactionReason): Boolean
	{
		if (amount <= 0) return true
		val currentEssence = getEssence(groupId)
		if (currentEssence < amount)
		{
			if(reason == TransactionReason.ADMIN_GIVE)
				dbManager.addSkyEssenceToGroup(groupId, -currentEssence)
			return false
		}
		dbManager.addSkyEssenceToGroup(groupId, -amount)
		return true
	}

	fun getEssence(groupId: Int): Int
	{
		return dbManager.getGroupSkyEssence(groupId)
	}

	fun getPassiveIncomeForGroup(groupId: Int): Int {
		return sourcesByGroup[groupId]?.values?.sumOf { it.amountPerSecond } ?: 0
	}

	private fun startPassiveIncomeLoop()
	{
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            try {
                for(groupId in sourcesByGroup.keys)
                {
                    val groupSources = sourcesByGroup[groupId] ?: continue
                    if(groupSources.isEmpty()) continue

                    val totalGainForGroup = groupSources.values.sumOf { it.amountPerSecond }

                    if(totalGainForGroup > 0)
					{
                        addEssence(groupId, totalGainForGroup, TransactionReason.PASSIVE_INCOME_TICK)
					}

//					Bukkit.getScheduler().runTask(plugin, Runnable {
//						val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
//						val objective = scoreboard.getObjective("se") ?: return@Runnable
//
//						objective.getScore(dbManager.getGroupById(groupId)!!.name)
//							.score = getEssence(groupId)
//					})
                }
            } catch (e : Exception) {
                plugin.logger.severe("Error in SkyEssenceManager passive income loop: ${e.message}")
                e.printStackTrace()
            }
        }, 20L, 20L)
	}

}