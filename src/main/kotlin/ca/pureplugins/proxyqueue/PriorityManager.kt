package ca.pureplugins.proxyqueue

import me.lucko.luckperms.api.LuckPermsApi
import me.lucko.luckperms.api.Tristate
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.config.Configuration
import java.util.function.Predicate

class PriorityManager(
    proxy: ProxyServer,
    private val luckPermsApi: LuckPermsApi,
    private val config: Configuration) {

    private val priorities = mutableListOf<PermissionPriority>()

    init {
        registerPermissions("global", config)

        proxy.servers.forEach { serverName, _ ->
            registerPermissions(serverName, config)
        }
    }

    private fun registerNode(server: String, key: String, priority: Int) {
        val node = luckPermsApi.buildNode(key)
            .setServer(server)
            .build()

        priorities += PermissionPriority(node, priority)
    }

    private fun registerPermissions(server: String, section: Configuration) {
        val priorities = section.keys
            .map { it to section.getStringList(it) }
            .map { it.first.toInt() to it.second }
            .toMap()

        priorities.forEach { priority, permissions ->
            permissions.forEach { permission ->
                registerNode(server, permission, priority)
            }
        }
    }

    fun getQueuePriority(player: ProxiedPlayer, server: ServerInfo): Int {
        val luckPermsUser = luckPermsApi.getUser(player.uniqueId) ?: return 0
        val predicate = Predicate<String> {it == "global" || it == server.name}

        val permissionPriority = priorities
            .filter { luckPermsUser.inheritsPermission(it.permission) == Tristate.TRUE }
            .filter { predicate.test(it.permission.server.orElse("global")) }
            .maxBy { it.priority }

        return permissionPriority?.priority ?: 0
    }
}