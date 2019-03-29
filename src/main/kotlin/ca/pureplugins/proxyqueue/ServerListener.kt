package ca.pureplugins.proxyqueue

import ca.pureplugins.proxyqueue.manager.PriorityManager
import ca.pureplugins.proxyqueue.manager.QueueManager
import ca.pureplugins.proxyqueue.util.PlayerLock
import ca.pureplugins.proxyqueue.util.toOrdinal
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class ServerListener(
    private val priorityManager: PriorityManager,
    private val queueManager: QueueManager,
    private val ignoredServers: List<String>,
    private val queueMessage: List<String>
) : Listener {

    private val playerLock = PlayerLock()

    @EventHandler
    fun onServerConnectEvent(event: ServerConnectEvent) {
        val player = event.player
        val targetServer = event.target

        // return if player is just connecting to proxy OR already on target server
        if (player.server == null || player.server.info == targetServer) {
            return
        }

        //server is ignored return
        if (ignoredServers.any { targetServer.name == it }) {
            return
        }

        // player is locked return
        if (playerLock.isLocked(player)) {
            return
        }

        event.isCancelled = true
        playerLock.lock(player)

        val queuePriority = priorityManager.getQueuePriority(player, targetServer)
        val queuePosition = queueManager.enqueuePlayer(player, targetServer, queuePriority)

        queueMessage.forEach { line ->
            player.sendMessage(
                TextComponent(
                    ChatColor.translateAlternateColorCodes(
                        '&',
                        line
                            .replace("\$position", queuePosition.toOrdinal())
                            .replace("\$server", targetServer.name)
                            .replace("\$priority", queuePriority.name)
                    )
                )
            )
        }
    }

    @EventHandler
    fun onServerConnectedEvent(event: ServerConnectedEvent) =
        playerLock.unlock(event.player)
}