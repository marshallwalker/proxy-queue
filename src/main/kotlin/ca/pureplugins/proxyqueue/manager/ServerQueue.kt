package ca.pureplugins.proxyqueue.manager

import ca.pureplugins.proxyqueue.model.PriorityLevel
import ca.pureplugins.proxyqueue.model.QueuedPlayer
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

class ServerQueue {
    private val queue = PriorityQueue<QueuedPlayer>()

    fun poll(server: ServerInfo) =
        queue.poll()?.connect(server)

    fun enqueue(player: ProxiedPlayer, priorityLevel: PriorityLevel): Int {
        val queuedPlayer = QueuedPlayer(player, priorityLevel)
        queue += queuedPlayer
        return queue.indexOf(queuedPlayer) + 1
    }
}