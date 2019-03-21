package ca.pureplugins.proxyqueue

import me.lucko.luckperms.api.LuckPermsApi
import me.lucko.luckperms.api.Tristate
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import java.util.function.Predicate

class PriorityManager(
    private val plugin: Plugin,
    private val luckPermsApi: LuckPermsApi,
    private val priorityLevels: List<PriorityLevel>) {

    private val defaultPriority = PriorityLevel(0, "", "default")
    private val permissions = mutableListOf<PriorityPermission>()

    init {
        registerPermissions("global")
        plugin.proxy.servers.keys.forEach(::registerPermissions)
    }

    private fun createPermissionNode(server: String, level: PriorityLevel) {
        plugin.logger.info("Creating permission node ${level.permission}, server=$server, priority=${level.priority}")

        val node = luckPermsApi.buildNode(level.permission)
            .setServer(server)
            .build()

        permissions += PriorityPermission(level, node)
    }

    private fun registerPermissions(server: String) {
        priorityLevels.forEach { level -> createPermissionNode(server, level) }
    }

    fun getQueuePriority(player: ProxiedPlayer, server: ServerInfo): PriorityLevel {
        val luckPermsUser = luckPermsApi.getUser(player.uniqueId) ?: return defaultPriority
        val predicate = Predicate<String> { it == "global" || it == server.name }

        return permissions
            .filter {
                luckPermsUser.inheritsPermission(it.node) == Tristate.TRUE && predicate.test(
                    it.node.server.orElse(
                        "global"
                    )
                )
            }
            .maxBy { it.level.priority }?.level ?: defaultPriority
    }
}