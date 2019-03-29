package ca.pureplugins.proxyqueue.model

class PriorityLevel(
    val priority: Int,
    val permission: String,
    val name: String) {

    constructor(map: Map<*, *>) : this(
        map["priority"] as Int,
        map["permission"] as String,
        map["name"] as String
    )
}