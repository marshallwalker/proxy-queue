package ca.pureplugins.proxyqueue

import me.lucko.luckperms.api.Node

class PriorityLevel(
    val priority: Int,
    val permission: String,
    val name: String) {

    lateinit var node: Node

    constructor(map: Map<String, Any>) : this(
        map["priority"] as Int,
        map["permission"] as String,
        map["name"] as String
    )
}