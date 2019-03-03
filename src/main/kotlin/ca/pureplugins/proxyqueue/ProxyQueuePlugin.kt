package ca.pureplugins.proxyqueue

import me.lucko.luckperms.LuckPerms
import me.lucko.luckperms.api.LuckPermsApi
import me.lucko.luckperms.api.Tristate
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import net.md_5.bungee.event.EventHandler
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class ProxyQueuePlugin : Plugin(), Listener {
    private val playerLock = mutableListOf<UUID>()
    private val priorityPermissions = mutableListOf<PermissionPriority>()

    private lateinit var queueManager: QueueManager
    private lateinit var permissionApi: LuckPermsApi
    private lateinit var enqueueMessage: String

    override fun onEnable() {
        permissionApi = LuckPerms.getApiSafe().orElseThrow {
            RuntimeException("Unable to get instance of LuckPermsApi.")
        }

        proxy.pluginManager.registerListener(this, this)

        val configFile = File(dataFolder, "config.yml")

        if(!configFile.exists()) {
            configFile.parentFile.mkdir()
            val resourceStream = javaClass.classLoader.getResourceAsStream(configFile.name)
            configFile.outputStream().use { resourceStream.copyTo(it) }
        }

        val provider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)
        val config = provider.load(configFile)

        val generalSection = config.getSection("general")
        val joinDelay = generalSection.getLong("join delay seconds")
        this.enqueueMessage = generalSection.getString("enqueue message")

        queueManager = QueueManager(this, proxy.scheduler)
        queueManager.start(joinDelay, TimeUnit.SECONDS)

        val prioritySection = config.getSection("priority")

        proxy.servers.keys.forEach { serverName ->
            registerPermissions(serverName, prioritySection)
        }
    }

    override fun onDisable() =
        queueManager.stop()

    private fun registerPermissions(server: String, section: Configuration) {
        val priorities = section.keys
            .map { it to section.getStringList(it) }
            .map { it.first.toInt() to it.second }
            .toMap()

        priorities.forEach { priority, permissions ->
            permissions.forEach { permission ->
                val node = permissionApi.buildNode(permission)
                    .setServer(server)
                    .build()

                priorityPermissions += PermissionPriority(node, priority)
            }
        }
    }

    private fun getOrdinal(number: Int): String {
        val mod100 = number % 100
        val mod10 = number % 10

        return "$number" + if (mod10 == 1 && mod100 != 11) {
            "st"
        } else if (mod10 == 2 && mod100 != 12) {
            "nd"
        } else if (mod10 == 3 && mod100 != 13) {
            "rd"
        } else {
            "th"
        }
    }

    @EventHandler
    fun onServerConnectEvent(event: ServerConnectEvent) {
        val player = event.player
        val target = event.target

        // return if player is connecting to proxy OR already on server
        if (player.server == null || player.server.info == target) {
            return
        }

        // return if player is locked
        if (playerLock.contains(player.uniqueId)) {
            return
        }

        val user = permissionApi.getUser(player.uniqueId)
        val permissionPriority = priorityPermissions
            .filter { user?.hasPermission(it.permission) == Tristate.TRUE }
            .filter { it.permission.server.orElse("") == target.name }
            .maxBy { it.priority }

        val priority = permissionPriority?.priority ?: 0

        val position = queueManager.enqueue(player, target, priority)

        player.sendMessage(TextComponent(ChatColor.translateAlternateColorCodes('&', enqueueMessage
            .replace("\$position", getOrdinal(position))
            .replace("\$priority", "$priority")
            .replace("\$server", target.name))))

        playerLock.add(player.uniqueId)
        event.isCancelled = true
    }

    @EventHandler
    fun onServerConnectedEvent(event: ServerConnectedEvent) =
        playerLock.removeIf { it == event.player.uniqueId }
}