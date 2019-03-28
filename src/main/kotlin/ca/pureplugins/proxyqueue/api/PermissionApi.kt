package ca.pureplugins.proxyqueue.api

import net.md_5.bungee.api.connection.ProxiedPlayer

interface PermissionApi {

    fun registerPermission(node: String, serverContext: String, priority: Int)

    fun hasPermission(player: ProxiedPlayer, node: String, serverContext: String): Boolean
}