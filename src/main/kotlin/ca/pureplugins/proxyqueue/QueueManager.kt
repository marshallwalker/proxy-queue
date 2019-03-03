package ca.pureplugins.proxyqueue

import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import net.md_5.bungee.api.scheduler.TaskScheduler
import java.util.concurrent.TimeUnit

class QueueManager(
    private val plugin: Plugin) {

    private val serverQueues = mutableMapOf<ServerInfo, ServerQueue>()
    private val scheduler = plugin.proxy.scheduler

    private lateinit var task: ScheduledTask

    private fun run() =
        serverQueues.forEach { server, queue -> queue.poll(server) }

    fun start(interval: Long, unit: TimeUnit) {
        task = scheduler.schedule(plugin, ::run, interval, interval, unit)
    }

    fun stop() =
        task.cancel()

    fun enqueue(player: ProxiedPlayer, server: ServerInfo, priority: Int) =
        serverQueues.computeIfAbsent(server) { ServerQueue() }.enqueue(player, priority)
}