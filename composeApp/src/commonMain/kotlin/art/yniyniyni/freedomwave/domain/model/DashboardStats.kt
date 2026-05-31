package art.yniyniyni.freedomwave.domain.model

import art.yniyniyni.freedomwave.data.api.dto.RecapData
import art.yniyniyni.freedomwave.data.api.dto.SystemStatsData

data class DashboardStats(
    val cpuCores: Int,
    val memoryUsedBytes: Long,
    val memoryTotalBytes: Long,
    val uptimeSeconds: Long,
    val totalUsers: Int,
    val activeUsers: Int,
    val onlineNow: Int,
    val onlineLastDay: Int,
    val onlineLastWeek: Int,
    val onlineNodes: Int,
    val totalNodes: Int,
    val monthTraffic: String,
    val totalTraffic: String,
    val nodesBytesLifetime: String,
    val panelVersion: String,
    val distinctCountries: Int
) {
    companion object {
        fun from(stats: SystemStatsData, recap: RecapData) = DashboardStats(
            cpuCores          = stats.cpu.cores,
            memoryUsedBytes   = stats.memory.used,
            memoryTotalBytes  = stats.memory.total,
            uptimeSeconds     = stats.uptime,
            totalUsers        = stats.users.totalUsers,
            activeUsers       = stats.users.statusCounts["ACTIVE"] ?: 0,
            onlineNow         = stats.onlineStats.onlineNow,
            onlineLastDay     = stats.onlineStats.lastDay,
            onlineLastWeek    = stats.onlineStats.lastWeek,
            onlineNodes       = stats.nodes.totalOnline,
            totalNodes        = recap.total.nodes,
            monthTraffic      = recap.thisMonth.traffic,
            totalTraffic      = recap.total.traffic,
            nodesBytesLifetime = stats.nodes.totalBytesLifetime,
            panelVersion      = recap.version,
            distinctCountries = recap.total.distinctCountries
        )
    }
}
