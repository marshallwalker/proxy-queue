package ca.pureplugins.proxyqueue

import me.lucko.luckperms.api.LuckPermsApi
import me.lucko.luckperms.api.Tristate
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.config.Configuration

class PriorityManager(
    proxy: ProxyServer,
    private val luckPermsApi: LuckPermsApi,
    private val config: Configuration) {

    private val priorities = mutableListOf<PermissionPriority>()

    init {
        proxy.servers.forEach { serverName, _ ->
            registerPermissions(serverName, config)
        }
    }

    private fun registerPermissions(server: String, section: Configuration) {
        val priorities = section.keys
            .map { it to section.getStringList(it) }
            .map { it.first.toInt() to it.second }
            .toMap()

        priorities.forEach { priority, permissions ->
            permissions.forEach { permission ->
                val node = luckPermsApi.buildNode(permission)
                    .setServer(server)
                    .build()

                this.priorities += PermissionPriority(node, priority)
            }
        }
    }

    fun getQueuePriority(player: ProxiedPlayer, server: ServerInfo): Int {
        val luckPermsUser = luckPermsApi.getUser(player.uniqueId) ?: return 0
        val permissionPriority = priorities
            .filter { luckPermsUser.hasPermission(it.permission) == Tristate.TRUE }
            .filter { it.permission.server.orElse("") == server.name }
            .maxBy { it.priority }

        return permissionPriority?.priority ?: 0
    }
}