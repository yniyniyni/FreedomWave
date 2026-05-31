package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.RecapResponse
import art.yniyniyni.freedomwave.data.api.dto.SystemStatsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class DashboardService(private val client: HttpClient) {
    suspend fun getSystemStats(serverUrl: String): SystemStatsResponse =
        client.get("$serverUrl/api/system/stats/system-stats").body()

    suspend fun getRecap(serverUrl: String): RecapResponse =
        client.get("$serverUrl/api/system/stats/recap").body()
}
