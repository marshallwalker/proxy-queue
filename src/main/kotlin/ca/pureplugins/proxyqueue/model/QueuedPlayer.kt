package ca.pureplugins.proxyqueue.model

import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer

data class QueuedPlayer(
    private val player: ProxiedPlayer,
    private val priorityLevel: PriorityLevel): Comparable<QueuedPlayer> {

    private val priority: Int get() = priorityLevel.priority

    fun connect(server: ServerInfo) =
        player.connect(server)

    override fun compareTo(other: QueuedPlayer) =
       Integer.compare(priority, other.priority)
}