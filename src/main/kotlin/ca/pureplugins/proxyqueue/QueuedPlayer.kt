package ca.pureplugins.proxyqueue

import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer

data class QueuedPlayer(
    private val player: ProxiedPlayer,
    private val priority: Int): Comparable<QueuedPlayer> {

    fun connect(server: ServerInfo) =
        player.connect(server)

    override fun compareTo(other: QueuedPlayer) =
       Integer.compare(priority, other.priority)
}