package ca.pureplugins.proxyqueue.manager

import ca.pureplugins.proxyqueue.model.PriorityLevel
import ca.pureplugins.proxyqueue.api.PermissionProvider
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin

class PriorityManager(
    private val plugin: Plugin,
    private val permissionApi: PermissionProvider,
    private val priorityLevels: List<PriorityLevel>) {

    private val defaultPriority = PriorityLevel(0, "", "default")

    init {
        registerPermissions("global")
        plugin.proxy.servers.keys.forEach(::registerPermissions)
    }

    private fun createPermissionNode(serverContext: String, level: PriorityLevel) {
        plugin.logger.info("Creating permission node ${level.permission}, server=$serverContext, priorityLevel=${level.priority}")
        permissionApi.registerPermission(level.permission, serverContext, level)
    }

    private fun registerPermissions(serverContext: String) =
        priorityLevels.forEach { level -> createPermissionNode(serverContext, level) }

    fun getQueuePriority(player: ProxiedPlayer, server: ServerInfo) = priorityLevels
        .filter { level -> permissionApi.hasPermission(player, level.permission, server.name) }
        .maxBy(PriorityLevel::priority) ?: defaultPriority
}