package ca.pureplugins.proxyqueue

import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

class ServerQueue {
    private val queue = PriorityQueue<QueuedPlayer>()

    fun poll(server: ServerInfo) {
        val queuedPlayer = queue.poll()
        queuedPlayer?.connect(server)
    }

    fun enqueue(player: ProxiedPlayer, priority: Int): Int {
        val queuedPlayer = QueuedPlayer(player, priority)
        queue.add(queuedPlayer)
        return queue.indexOf(queuedPlayer) + 1
    }
}