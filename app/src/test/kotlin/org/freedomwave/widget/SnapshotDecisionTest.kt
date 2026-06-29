package org.freedomwave.widget

import org.freedomwave.domain.model.DashboardStats
import kotlin.test.assertEquals
import org.junit.Test

class SnapshotDecisionTest {

    private fun stats(online: Int) = DashboardStats(
        cpuCores = 1, memoryUsedBytes = 0, memoryTotalBytes = 0, uptimeSeconds = 0,
        totalUsers = 10, activeUsers = 9, onlineNow = online,
        onlineLastDay = 0, onlineLastWeek = 0, onlineNodes = 1, totalNodes = 1,
        monthTraffic = "", totalTraffic = "", nodesBytesLifetime = "",
        panelVersion = "", distinctCountries = 0, neverOnline = 0,
        statusCounts = emptyMap(), todayBytes = 0L
    )

    @Test fun no_key_yields_not_connected() {
        val s = decideSnapshot(apiKeyPresent = false, result = Result.success(stats(5)), nowMs = 2L, previous = null)
        assertEquals(Status.NotConnected, s.status)
    }

    @Test fun success_yields_ok() {
        val s = decideSnapshot(apiKeyPresent = true, result = Result.success(stats(5)), nowMs = 2L, previous = null)
        assertEquals(Status.Ok, s.status)
        assertEquals(5, s.onlineNow)
    }

    @Test fun failure_keeps_previous_data_marked_error() {
        val prev = WidgetSnapshot(onlineNow = 7, updatedAtEpochMs = 1L, status = Status.Ok)
        val s = decideSnapshot(apiKeyPresent = true, result = Result.failure(RuntimeException()), nowMs = 9L, previous = prev)
        assertEquals(Status.Error, s.status)
        assertEquals(7, s.onlineNow)
        assertEquals(1L, s.updatedAtEpochMs) // keeps the old "last good" timestamp
    }

    @Test fun failure_without_previous_is_empty_error() {
        val s = decideSnapshot(apiKeyPresent = true, result = Result.failure(RuntimeException()), nowMs = 9L, previous = null)
        assertEquals(Status.Error, s.status)
        assertEquals(0, s.onlineNow)
    }
}
