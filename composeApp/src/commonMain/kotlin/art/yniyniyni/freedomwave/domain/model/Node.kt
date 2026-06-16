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
    val memoryFreeBytes: Long?,
    val uptimeSeconds: Long?,
    val loadAvg1: Float?,
    val loadAvg5: Float?,
    val loadAvg15: Float?,
    val hostname: String?,
    val cpuModel: String?,
    val arch: String?,
    val platform: String?,
    val usersOnline: Int,
    val xrayVersion: String?,
    val nodeVersion: String?,
    val xrayUptimeSeconds: Long?,
    val isTrafficTrackingActive: Boolean,
    val trafficResetDay: Int?,
    val notifyPercent: Int?,
    val consumptionMultiplier: Double,
    val lastStatusChange: String?,
    val lastStatusMessage: String?,
    val createdAt: String,
    val updatedAt: String,
    val activeConfigProfileUuid: String?,
    val activeInbounds: List<String>,
) {
    val isOnline: Boolean   get() = status == NodeStatus.ONLINE
    val isDisabled: Boolean get() = status == NodeStatus.DISABLED

    /** Load-based CPU approximation: loadAvg(1m) / cores. Same metric the panel shows. */
    val cpuLoadPercent: Float?
        get() {
            val load = loadAvg1 ?: return null
            val n = cpus ?: return null
            if (n <= 0) return null
            return (load / n * 100f).coerceIn(0f, 100f)
        }

    val memoryUsedPercent: Float?
        get() {
            val used = memoryUsedBytes ?: return null
            val total = memoryTotalBytes ?: return null
            if (total <= 0) return null
            return (used.toFloat() / total.toFloat() * 100f).coerceIn(0f, 100f)
        }

    companion object {
        fun from(dto: NodeDto) = Node(
            uuid              = dto.uuid,
            name              = dto.name,
            address           = dto.address,
            port              = dto.port,
            status            = when {
                dto.isDisabled   -> NodeStatus.DISABLED
                dto.isConnecting -> NodeStatus.CONNECTING
                dto.isConnected  -> NodeStatus.ONLINE
                else             -> NodeStatus.OFFLINE
            },
            countryCode       = dto.countryCode,
            tags              = dto.tags,
            trafficUsedBytes  = dto.trafficUsedBytes ?: 0L,
            trafficLimitBytes = dto.trafficLimitBytes,
            cpus              = dto.system?.info?.cpus,
            memoryUsedBytes   = dto.system?.stats?.memoryUsed,
            memoryTotalBytes  = dto.system?.info?.memoryTotal,
            memoryFreeBytes   = dto.system?.stats?.memoryFree,
            uptimeSeconds     = dto.system?.stats?.uptime?.toLong(),
            loadAvg1          = dto.system?.stats?.loadAvg?.getOrNull(0)?.toFloat(),
            loadAvg5          = dto.system?.stats?.loadAvg?.getOrNull(1)?.toFloat(),
            loadAvg15         = dto.system?.stats?.loadAvg?.getOrNull(2)?.toFloat(),
            hostname          = dto.system?.info?.hostname,
            cpuModel          = dto.system?.info?.cpuModel,
            arch              = dto.system?.info?.arch,
            platform          = dto.system?.info?.platform,
            usersOnline       = dto.usersOnline,
            xrayVersion       = dto.versions?.xray,
            nodeVersion       = dto.versions?.node,
            xrayUptimeSeconds = if (dto.xrayUptime > 0) dto.xrayUptime.toLong() else null,
            isTrafficTrackingActive = dto.isTrafficTrackingActive,
            trafficResetDay   = dto.trafficResetDay,
            notifyPercent     = dto.notifyPercent,
            consumptionMultiplier = dto.consumptionMultiplier,
            lastStatusChange  = dto.lastStatusChange,
            lastStatusMessage = dto.lastStatusMessage,
            createdAt         = dto.createdAt,
            updatedAt         = dto.updatedAt,
            activeConfigProfileUuid = dto.configProfile?.activeConfigProfileUuid,
            activeInbounds          = dto.configProfile?.activeInbounds ?: emptyList(),
        )
    }
}
