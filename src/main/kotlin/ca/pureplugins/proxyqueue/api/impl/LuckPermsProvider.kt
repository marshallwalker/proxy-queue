package ca.pureplugins.proxyqueue.api.impl

import ca.pureplugins.proxyqueue.api.PermissionProvider
import ca.pureplugins.proxyqueue.model.PermissionPriority
import ca.pureplugins.proxyqueue.model.PriorityLevel
import me.lucko.luckperms.api.LuckPermsApi
import me.lucko.luckperms.api.Node
import net.md_5.bungee.api.connection.ProxiedPlayer

class LuckPermsProvider(
    private val api: LuckPermsApi) : PermissionProvider {

    private val permissions = mutableListOf<PermissionPriority<Node>>()

    override fun registerPermission(node: String, serverContext: String, priority: PriorityLevel) {
        val permission = api.buildNode(node).setServer(serverContext).build()
        permissions += PermissionPriority(permission, priority)
    }

    override fun hasPermission(player: ProxiedPlayer, node: String, serverContext: String): Boolean {
        val user = api.getUser(player.uniqueId) ?: return false

        return permissions.any { (permission, _) ->
            val hasPermission = user.inheritsPermission(permission).asBoolean()
            val server = permission.server.orElse("global")

            hasPermission && (server == "global" || server == serverContext)
        }
    }
}