package art.yniyniyni.freedomwave.widget

import art.yniyniyni.freedomwave.domain.model.DashboardStats
import art.yniyniyni.freedomwave.util.byteValue
import art.yniyniyni.freedomwave.util.format2
import kotlinx.serialization.Serializable

enum class Status { Loading, Ok, NotConnected, Error }

@Serializable
data class WidgetSnapshot(
    val onlineNow: Int = 0,
    val nodesOnline: Int = 0,
    val nodesTotal: Int = 0,
    val activeUsers: Int = 0,
    val totalUsers: Int = 0,
    val trafficLabel: String = "—",
    val updatedAtEpochMs: Long = 0L,
    val status: Status = Status.Loading
) {
    companion object {
        fun from(stats: DashboardStats, nowMs: Long) = WidgetSnapshot(
            onlineNow = stats.onlineNow,
            nodesOnline = stats.onlineNodes,
            nodesTotal = stats.totalNodes,
            activeUsers = stats.activeUsers,
            totalUsers = stats.totalUsers,
            trafficLabel = formatTrafficLabel(stats.todayBytes),
            updatedAtEpochMs = nowMs,
            status = Status.Ok
        )

        fun loading() = WidgetSnapshot(status = Status.Loading)

        fun notConnected(nowMs: Long) = WidgetSnapshot(
            updatedAtEpochMs = nowMs,
            status = Status.NotConnected
        )
    }
}

/** Scales a byte count into its largest fitting unit, e.g. "1.00 GB". */
fun formatTrafficLabel(bytes: Long): String {
    val bv = byteValue(bytes)
    return "${bv.value.format2()} ${bv.unit.name}"
}

/** Coarse "time since" label for the widget footer: now / 5m / 2h / 3d. */
fun relativeTimeLabel(thenMs: Long, nowMs: Long): String {
    val seconds = ((nowMs - thenMs) / 1000).coerceAtLeast(0)
    return when {
        seconds < 60 -> "now"
        seconds < 3_600 -> "${seconds / 60}m"
        seconds < 86_400 -> "${seconds / 3_600}h"
        else -> "${seconds / 86_400}d"
    }
}
