package ca.pureplugins.proxyqueue

import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class ServerListener(
    private val priorityManager: PriorityManager,
    private val queueManager: QueueManager) : Listener {

    private val playerLock = PlayerLock()

    private fun getOrdinal(number: Int): String {
        val mod100 = number % 100
        val mod10 = number % 10

        return "$number" + if (mod10 == 1 && mod100 != 11) {
            "st"
        } else if (mod10 == 2 && mod100 != 12) {
            "nd"
        } else if (mod10 == 3 && mod100 != 13) {
            "rd"
        } else {
            "th"
        }
    }

    @EventHandler
    fun onServerConnectEvent(event: ServerConnectEvent) {
        val player = event.player
        val target = event.target

        // return if player is just connecting to proxy OR already on server target server
        if (player.server == null || player.server.info == target) {
            return
        }

        // return if player is locked
        if (playerLock.isLocked(player)) {
            return
        }

        event.isCancelled = true
        playerLock.lock(player)

        val queuePriority = priorityManager.getQueuePriority(player, target)
        val queuePosition = queueManager.enqueue(player, target, queuePriority)

        player.sendMessage("You are ${getOrdinal(queuePosition)} to join ${target.name} at priority $queuePriority")
    }

    @EventHandler
    fun onServerConnectedEvent(event: ServerConnectedEvent) =
        playerLock.unlock(event.player)
}