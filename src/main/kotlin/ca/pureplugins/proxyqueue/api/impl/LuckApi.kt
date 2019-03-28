package ca.pureplugins.proxyqueue.api.impl

import ca.pureplugins.proxyqueue.api.PermissionApi
import ca.pureplugins.proxyqueue.api.PermissionPriority
import me.lucko.luckperms.api.LuckPermsApi
import me.lucko.luckperms.api.Node
import me.lucko.luckperms.api.Tristate
import net.md_5.bungee.api.connection.ProxiedPlayer

class LuckApi(
    private val api: LuckPermsApi): PermissionApi {

    private val permissionNodes = mutableListOf<PermissionPriority<Node>>()

    override fun registerPermission(node: String, serverContext: String, priority: Int) {
        val permission = api.buildNode(node).setServer(serverContext).build()
        permissionNodes += PermissionPriority(permission, priority)
    }

    override fun hasPermission(player: ProxiedPlayer, node: String, serverContext: String): Boolean {
        val user = api.getUser(player.uniqueId) ?: return false
        return permissionNodes.any { user.inheritsPermission(it.node) == Tristate.TRUE }
    }
}