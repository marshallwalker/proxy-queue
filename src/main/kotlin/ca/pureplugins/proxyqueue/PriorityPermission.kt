package ca.pureplugins.proxyqueue

import me.lucko.luckperms.api.Node

data class PriorityPermission(
    val level: PriorityLevel,
    val node: Node
)