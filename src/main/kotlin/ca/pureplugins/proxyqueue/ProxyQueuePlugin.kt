package ca.pureplugins.proxyqueue

import ca.pureplugins.proxyqueue.api.BungeeCordPermissionApi
import ca.pureplugins.proxyqueue.api.LuckPermsPermissionApi
import ca.pureplugins.proxyqueue.api.PermissionApi
import ca.pureplugins.proxyqueue.manager.PriorityManager
import ca.pureplugins.proxyqueue.manager.QueueManager
import ca.pureplugins.proxyqueue.model.PriorityLevel
import me.lucko.luckperms.LuckPerms
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.util.concurrent.TimeUnit

class ProxyQueuePlugin : Plugin(), Listener {
    private lateinit var queueManager: QueueManager

    override fun onEnable() {
        var permissionApi: PermissionApi? = null

        LuckPerms.getApiSafe().ifPresent {
            logger.info("LuckPerms detected, hooking!")
            permissionApi = LuckPermsPermissionApi(it)
        }

        if (permissionApi == null) {
            logger.info("no 3rd party permissions detected, using bungee permissions.")
            permissionApi = BungeeCordPermissionApi()
            return
        }

        val configFile = File(dataFolder, "config.yml")

        if (!configFile.exists()) {
            logger.info("Creating default config.yml")
            configFile.parentFile.mkdir()
            val resourceStream = javaClass.classLoader.getResourceAsStream(configFile.name)
            configFile.outputStream().use { resourceStream.copyTo(it) }
        }

        val configurationProvider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)
        val configuration = configurationProvider.load(configFile)

        val generalSection = configuration.getSection("general")
        val joinDelay = generalSection.getLong("join delay seconds")
        val ignoredServers = generalSection.getStringList("ignore")
        val queueMessages = generalSection.getStringList("enqueue message")

        this.queueManager = QueueManager(this)
            .start(joinDelay, TimeUnit.SECONDS)

        val prioritySection = configuration.getList("levels")
            .map { PriorityLevel(it as Map<*, *>) }

        val priorityManager = PriorityManager(this, permissionApi!!, prioritySection)
        val serverListener = ServerListener(priorityManager, queueManager, ignoredServers, queueMessages)

        proxy.pluginManager.registerListener(this, serverListener)
    }

    override fun onDisable() =
        queueManager.stop()
}