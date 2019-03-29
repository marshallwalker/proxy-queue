package ca.pureplugins.proxyqueue.model

data class PermissionPriority<T>(
    val node: T,
    val priorityLevel: PriorityLevel)