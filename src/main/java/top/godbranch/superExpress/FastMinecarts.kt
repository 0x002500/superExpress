package top.godbranch.superExpress

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.bukkit.plugin.java.JavaPlugin

class FastMinecarts : JavaPlugin(), Listener {
    private val vanillaMaxSpeed = 0.4
    private var _blockMaxSpeeds = mutableMapOf<Material, Double>()
    private val railTypes = listOf(
        Material.RAIL, Material.POWERED_RAIL,
        Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL
    )

    override fun onEnable() {
        saveDefaultConfig()
        loadConfig()
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    private fun loadConfig() {
        val blockConfig = config.getConfigurationSection("blocks") ?: return
        _blockMaxSpeeds.clear()
        for (key in blockConfig.getKeys(false)) {
            val material = Material.getMaterial(key)
            if (material != null) {
                _blockMaxSpeeds[material] = blockConfig.getDouble(key)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onVehicleMove(event: VehicleMoveEvent) {
        if (event.vehicle !is Minecart) return

        val minecart = event.vehicle as Minecart
        if (minecart.isEmpty) return
        if (minecart.passengers.first() !is Player) return

        val railBlock = event.vehicle.location.block
        if (railBlock.type !in railTypes) return

        val blockBelow = railBlock.getRelative(0, -1, 0)
        val blockMultiplier = _blockMaxSpeeds[blockBelow.type] ?: vanillaMaxSpeed

        // Use Folia's region scheduler to update minecart speed on the main thread
        FoliaScheduler.callSyncMethod(this) {
            minecart.maxSpeed = blockMultiplier
        }
    }



    @EventHandler(ignoreCancelled = true)
    fun onVehicleExit(event: VehicleExitEvent) {
        if (event.vehicle !is Minecart) return
        if (event.exited !is Player) return

        val minecart = event.vehicle as Minecart
        if (minecart.maxSpeed > vanillaMaxSpeed) {
            // Use Folia's region scheduler to reset minecart speed on the main thread
            FoliaScheduler.callSyncMethod(this) {
                minecart.maxSpeed = vanillaMaxSpeed
            }
        }
    }

    object FoliaScheduler {
        fun callSyncMethod(plugin: JavaPlugin, task: () -> Unit) {
            if (Bukkit.isPrimaryThread()) {
                task()
            } else {
                Bukkit.getScheduler().runTask(plugin, Runnable { task() })
            }
        }
    }
}
