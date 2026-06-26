package art.yniyniyni.freedomwave.widget

import art.yniyniyni.freedomwave.domain.model.DashboardStats
import kotlin.test.assertEquals
import org.junit.Test

class WidgetSnapshotTest {

    private fun stats() = DashboardStats(
        cpuCores = 4, memoryUsedBytes = 0, memoryTotalBytes = 0, uptimeSeconds = 0,
        totalUsers = 100, activeUsers = 80, onlineNow = 12,
        onlineLastDay = 0, onlineLastWeek = 0, onlineNodes = 4, totalNodes = 5,
        monthTraffic = "", totalTraffic = "", nodesBytesLifetime = "",
        panelVersion = "", distinctCountries = 0, neverOnline = 0,
        statusCounts = emptyMap(), todayBytes = 1_073_741_824L
    )

    @Test fun from_maps_priority_stats_and_marks_ok() {
        val s = WidgetSnapshot.from(stats(), nowMs = 1_000L)
        assertEquals(12, s.onlineNow)
        assertEquals(4, s.nodesOnline)
        assertEquals(5, s.nodesTotal)
        assertEquals(80, s.activeUsers)
        assertEquals(100, s.totalUsers)
        assertEquals("1.00 GB", s.trafficLabel)
        assertEquals(1_000L, s.updatedAtEpochMs)
        assertEquals(Status.Ok, s.status)
    }

    @Test fun traffic_label_scales_to_largest_unit() {
        assertEquals("0.00 B", formatTrafficLabel(0L))
        assertEquals("512.00 MB", formatTrafficLabel(512L * 1_048_576L))
    }

    @Test fun relative_time_buckets() {
        assertEquals("now", relativeTimeLabel(thenMs = 0L, nowMs = 30_000L))
        assertEquals("5m", relativeTimeLabel(thenMs = 0L, nowMs = 5 * 60_000L))
        assertEquals("2h", relativeTimeLabel(thenMs = 0L, nowMs = 2 * 3_600_000L))
        assertEquals("3d", relativeTimeLabel(thenMs = 0L, nowMs = 3 * 86_400_000L))
    }

    @Test fun not_connected_factory() {
        val s = WidgetSnapshot.notConnected(nowMs = 7L)
        assertEquals(Status.NotConnected, s.status)
        assertEquals(7L, s.updatedAtEpochMs)
    }
}
