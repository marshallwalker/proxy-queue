package ca.pureplugins.proxyqueue

import me.lucko.luckperms.api.LuckPermsApi
import me.lucko.luckperms.api.Tristate
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.function.Predicate

class PriorityManager(
    proxy: ProxyServer,
    private val luckPermsApi: LuckPermsApi,
    private val priorityLevels: List<PriorityLevel>) {

    private val defaultPriority = PriorityLevel(0, "", "default")

    init {
        registerPermissions("global")
        proxy.servers.keys.forEach(::registerPermissions)
    }

    private fun createPermissionNode(server: String, level: PriorityLevel) {
        level.node = luckPermsApi.buildNode(level.permission)
            .setServer(server)
            .build()
    }

    private fun registerPermissions(server: String) {
        priorityLevels.forEach { level -> createPermissionNode(server, level) }
    }

    fun getQueuePriority(player: ProxiedPlayer, server: ServerInfo): PriorityLevel {
        val luckPermsUser = luckPermsApi.getUser(player.uniqueId) ?: return defaultPriority
        val predicate = Predicate<String> { it == "global" || it == server.name }

        val permissionPriority = priorityLevels
            .filter { luckPermsUser.inheritsPermission(it.node) == Tristate.TRUE }
            .filter { predicate.test(it.node.server.orElse("global")) }
            .maxBy { it.priority }

        return permissionPriority ?: defaultPriority
    }
}