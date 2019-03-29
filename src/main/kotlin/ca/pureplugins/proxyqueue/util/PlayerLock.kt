package ca.pureplugins.proxyqueue.util

import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

class PlayerLock {
    private val lockedUniqueIds = mutableListOf<UUID>()

    fun lock(player: ProxiedPlayer) =
        lockedUniqueIds.add(player.uniqueId)

    fun unlock(player: ProxiedPlayer) =
        lockedUniqueIds.remove(player.uniqueId)

    fun isLocked(player: ProxiedPlayer) =
        lockedUniqueIds.contains(player.uniqueId)
}