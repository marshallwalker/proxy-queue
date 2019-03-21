package ca.pureplugins.proxyqueue

class PriorityLevel(
    val priority: Int,
    val permission: String,
    val name: String) {

    constructor(map: Map<String, Any>) : this(
        map["priority"] as Int,
        map["permission"] as String,
        map["name"] as String
    )
}