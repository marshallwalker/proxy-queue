package ca.pureplugins.proxyqueue.api.impl

import ca.pureplugins.proxyqueue.api.PermissionProvider
import ca.pureplugins.proxyqueue.model.PriorityLevel
import net.md_5.bungee.api.connection.ProxiedPlayer

class BungeeCordProvider : PermissionProvider {

    override fun registerPermission(node: String, serverContext: String, priority: PriorityLevel) {
        //nothing to do here
    }

    override fun hasPermission(player: ProxiedPlayer, node: String, serverContext: String) =
        player.hasPermission("$node:$serverContext")
}
