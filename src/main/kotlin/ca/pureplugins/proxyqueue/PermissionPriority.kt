package ca.pureplugins.proxyqueue

import me.lucko.luckperms.api.Node

data class PermissionPriority(
    val permission: Node,
    val priority: Int)