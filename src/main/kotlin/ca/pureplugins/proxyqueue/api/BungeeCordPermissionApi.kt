package ca.pureplugins.proxyqueue.api

import ca.pureplugins.proxyqueue.model.PriorityLevel
import net.md_5.bungee.api.connection.ProxiedPlayer

class BungeeCordPermissionApi : PermissionApi {

    override fun registerPermission(node: String, serverContext: String, priority: PriorityLevel) {
        //nothing to do here
    }

    override fun hasPermission(player: ProxiedPlayer, node: String, serverContext: String) =
        player.hasPermission("$node:$serverContext")
}
