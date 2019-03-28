package ca.pureplugins.proxyqueue

import ca.pureplugins.proxyqueue.api.PermissionApi
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin

class PriorityManager(
    private val plugin: Plugin,
    private val permissionApi: PermissionApi,
    private val priorityLevels: List<PriorityLevel>) {

    private val defaultPriority = PriorityLevel(0, "", "default")

    init {
        registerPermissions("global")
        plugin.proxy.servers.keys.forEach(::registerPermissions)
    }

    private fun createPermissionNode(serverContext: String, level: PriorityLevel) {
        plugin.logger.info("Creating permission node ${level.permission}, server=$serverContext, priority=${level.priority}")
        permissionApi.registerPermission(level.permission, serverContext, level.priority)
    }

    private fun registerPermissions(serverContext: String) =
        priorityLevels.forEach { level -> createPermissionNode(serverContext, level) }

    fun getQueuePriority(player: ProxiedPlayer, server: ServerInfo) = priorityLevels
        .filter { level -> permissionApi.hasPermission(player, level.permission, server.name) }
        .maxBy { it.priority } ?: defaultPriority
}