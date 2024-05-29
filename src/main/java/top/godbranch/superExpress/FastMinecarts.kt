package top.godbranch.superExpress

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class FastMinecarts : JavaPlugin(), Listener {
    private val vanillaMaxSpeed = 0.4
    private var _blockMaxSpeeds = mutableMapOf<Material, Double>()
    private val railTypes = listOf(
        Material.RAIL, Material.POWERED_RAIL,
        Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL
    )
    private val playerActionBarTasks = mutableMapOf<Player, BukkitRunnable>()

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

        // Update minecart speed on the main thread
        FoliaScheduler.callSyncMethod(this) {
            minecart.maxSpeed = blockMultiplier
        }
        val velocity = minecart.velocity
        // Send long-term action bar message
        displayMinecartsSpeed(minecart.passengers.first() as Player, "Speed: ${velocity.length()}", 100) // Display for 5 seconds
    }

    private fun displayMinecartsSpeed(player: Player, message: String, durationTicks: Long) {
        playerActionBarTasks[player]?.cancel()

        val runnable = object : BukkitRunnable() {
            var ticksLeft = durationTicks

            override fun run() {
                if (ticksLeft <= 0) {
                    cancel()
                    playerActionBarTasks.remove(player)
                    return
                }

                player.sendActionBar(Component.text(message, NamedTextColor.YELLOW))
                ticksLeft -= 20
            }
        }

        runnable.runTaskTimer(this, 0, 20)
        playerActionBarTasks[player] = runnable
    }

    @EventHandler(ignoreCancelled = true)
    fun onVehicleExit(event: VehicleExitEvent) {
        if (event.vehicle !is Minecart) return
        if (event.exited !is Player) return

        val minecart = event.vehicle as Minecart
        if (minecart.maxSpeed > vanillaMaxSpeed) {
            // Reset minecart speed on the main thread
            FoliaScheduler.callSyncMethod(this) {
                minecart.maxSpeed = vanillaMaxSpeed
            }
        }
    }

    object FoliaScheduler {
        // Ensure tasks are executed on the main thread
        fun callSyncMethod(plugin: JavaPlugin, task: () -> Unit) {
            if (Bukkit.isPrimaryThread()) {
                task()
            } else {
                Bukkit.getScheduler().runTask(plugin, Runnable { task() })
            }
        }
    }
}

