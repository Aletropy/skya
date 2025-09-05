import com.aletropy.skya.Skya
import org.bukkit.configuration.file.FileConfiguration
import kotlin.math.pow

object BalanceConfig
{
	private lateinit var config : FileConfiguration

	private var genUpgradeCostBase : Double = 100.0
	private var genUpgradeCostGrowth : Double = 1.15
	private var genProductionBase : Int = 1

	fun load(plugin : Skya) {
		plugin.saveDefaultConfig()
		config = plugin.config

		genUpgradeCostBase = config.getDouble("balance.generator.upgrade-cost-base", 100.0)
		genUpgradeCostGrowth = config.getDouble("balance.generator.upgrade-cost-growth-factor", 1.15)
		genProductionBase = config.getInt("balance.generator.production-base")
	}

	fun getGeneratorUpgradeCost(currentLevel : Int) : Long {
		return (genUpgradeCostBase * genUpgradeCostGrowth.pow(currentLevel)).toLong()
	}

	fun getGeneratorProduction(level: Int): Int {
		return genProductionBase * level
	}
}