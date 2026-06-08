package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.BandwidthStatsResponse
import art.yniyniyni.freedomwave.data.api.dto.RecapResponse
import art.yniyniyni.freedomwave.data.api.dto.SystemStatsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class DashboardService(private val client: HttpClient) {
    suspend fun getSystemStats(serverUrl: String): SystemStatsResponse =
        client.get("$serverUrl/api/system/stats").body()

    suspend fun getRecap(serverUrl: String): RecapResponse =
        client.get("$serverUrl/api/system/stats/recap").body()

    suspend fun getBandwidthStats(serverUrl: String, tz: String? = null): BandwidthStatsResponse =
        client.get("$serverUrl/api/system/stats/bandwidth") {
            if (tz != null) parameter("tz", tz)
        }.body()
}
