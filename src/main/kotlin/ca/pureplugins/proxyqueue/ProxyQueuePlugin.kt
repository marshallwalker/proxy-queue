package ca.pureplugins.proxyqueue

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
        val luckPermsApi = LuckPerms.getApiSafe().orElseThrow {
            RuntimeException("Unable to get instance of LuckPermsApi.")
        }

        val configFile = File(dataFolder, "config.yml")

        if(!configFile.exists()) {
            configFile.parentFile.mkdir()
            val resourceStream = javaClass.classLoader.getResourceAsStream(configFile.name)
            configFile.outputStream().use { resourceStream.copyTo(it) }
        }

        val configurationProvider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)
        val configuration = configurationProvider.load(configFile)

        val generalSection = configuration.getSection("general")
        val joinDelay = generalSection.getLong("join delay seconds")
        //this.enqueueMessage = generalSection.getString("enqueue message")

        val queueManager = QueueManager(this)
        queueManager.start(joinDelay, TimeUnit.SECONDS)

        val prioritySection = configuration.getSection("priority")
        val priorityManager = PriorityManager(proxy, luckPermsApi, prioritySection)

        proxy.pluginManager.registerListener(this, ServerListener(priorityManager, queueManager))
    }

    override fun onDisable() =
        queueManager.stop()
}