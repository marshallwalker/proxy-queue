package ca.pureplugins.proxyqueue.api

import ca.pureplugins.proxyqueue.model.PriorityLevel
import net.md_5.bungee.api.connection.ProxiedPlayer

interface PermissionApi {

    fun registerPermission(node: String, serverContext: String, priority: PriorityLevel)

    fun hasPermission(player: ProxiedPlayer, node: String, serverContext: String): Boolean
}