package org.freedomwave.data.api.service

import org.freedomwave.data.api.dto.BandwidthStatsResponse
import org.freedomwave.data.api.dto.RecapResponse
import org.freedomwave.data.api.dto.SystemStatsResponse
import org.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class DashboardService(private val client: HttpClient, private val prefs: AppPreferences) {
    suspend fun getSystemStats(): SystemStatsResponse =
        client.get("${prefs.getServerUrl()}/api/system/stats").body()

    suspend fun getRecap(): RecapResponse =
        client.get("${prefs.getServerUrl()}/api/system/stats/recap").body()

    suspend fun getBandwidthStats(tz: String? = null): BandwidthStatsResponse =
        client.get("${prefs.getServerUrl()}/api/system/stats/bandwidth") {
            if (tz != null) parameter("tz", tz)
        }.body()
}
