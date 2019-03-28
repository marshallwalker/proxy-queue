package ca.pureplugins.proxyqueue

import ca.pureplugins.proxyqueue.api.impl.LuckApi
import ca.pureplugins.proxyqueue.api.PermissionApi
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
            permissionApi = LuckApi(it)
        }

        if(permissionApi == null) {
            proxy.stop("Unable to get instance of permissions plugin.")
            return
        }

        val configFile = File(dataFolder, "config.yml")

        if (!configFile.exists()) {
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

        val queueManager = QueueManager(this)
        queueManager.start(joinDelay, TimeUnit.SECONDS)

        val prioritySection = configuration.getList("levels")
            .map { PriorityLevel(it as Map<String, Any>) }

        val priorityManager = PriorityManager(this, permissionApi!!, prioritySection)
        proxy.pluginManager.registerListener(this, ServerListener(priorityManager, queueManager, ignoredServers, queueMessages))
    }

    override fun onDisable() =
        queueManager.stop()
}