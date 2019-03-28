package ca.pureplugins.proxyqueue.api

data class PermissionPriority<T>(
    val node: T,
    val priority: Int)