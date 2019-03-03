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
    private lateinit var queueManager: QueueManager

    override fun onEnable() {
        val permissionApi = LuckPerms.getApiSafe().orElseThrow {
            RuntimeException("Unable to get instance of LuckPermsApi.")
        }

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
        //this.enqueueMessage = generalSection.getString("enqueue message")

        val queueManager = QueueManager(this, proxy.scheduler)
        queueManager.start(joinDelay, TimeUnit.SECONDS)

        val prioritySection = config.getSection("priority")

        val priorityManager = PriorityManager(proxy, permissionApi, prioritySection)
        proxy.pluginManager.registerListener(this, ServerListener(priorityManager, queueManager))
    }

    override fun onDisable() =
        queueManager.stop()
}