package art.yniyniyni.freedomwave.domain.model

import art.yniyniyni.freedomwave.data.api.dto.NodeDto

enum class NodeStatus { ONLINE, OFFLINE, DISABLED, CONNECTING }

data class Node(
    val uuid: String,
    val name: String,
    val address: String,
    val port: Int?,
    val status: NodeStatus,
    val countryCode: String,
    val tags: List<String>,
    val trafficUsedBytes: Long,
    val trafficLimitBytes: Long?,
    val cpus: Int?,
    val memoryUsedBytes: Long?,
    val memoryTotalBytes: Long?,
    val uptimeSeconds: Long?,
    val loadAvg: Float?,
    val hostname: String?,
    val lastStatusMessage: String?,
    val createdAt: String
) {
    val isOnline: Boolean   get() = status == NodeStatus.ONLINE
    val isDisabled: Boolean get() = status == NodeStatus.DISABLED

    companion object {
        fun from(dto: NodeDto) = Node(
            uuid              = dto.uuid,
            name              = dto.name,
            address           = dto.address,
            port              = dto.port,
            status            = when {
                dto.isDisabled  -> NodeStatus.DISABLED
                dto.isConnecting -> NodeStatus.CONNECTING
                dto.isConnected -> NodeStatus.ONLINE
                else            -> NodeStatus.OFFLINE
            },
            countryCode       = dto.countryCode,
            tags              = dto.tags,
            trafficUsedBytes  = dto.trafficUsedBytes ?: 0L,
            trafficLimitBytes = dto.trafficLimitBytes,
            cpus              = dto.system?.info?.cpus,
            memoryUsedBytes   = dto.system?.stats?.memoryUsed,
            memoryTotalBytes  = dto.system?.info?.memoryTotal,
            uptimeSeconds     = dto.system?.stats?.uptime,
            loadAvg           = dto.system?.stats?.loadAvg?.firstOrNull()?.toFloat(),
            hostname          = dto.system?.info?.hostname,
            lastStatusMessage = dto.lastStatusMessage,
            createdAt         = dto.createdAt
        )
    }
}
