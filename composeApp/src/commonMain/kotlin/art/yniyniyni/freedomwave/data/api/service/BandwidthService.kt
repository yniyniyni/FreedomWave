package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.BandwidthNodesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class BandwidthService(private val client: HttpClient) {

    suspend fun getNodesStats(
        serverUrl: String,
        start: String,
        end: String,
        topNodesLimit: Int = 20
    ): BandwidthNodesResponse =
        client.get("$serverUrl/api/bandwidth-stats/nodes") {
            parameter("start", start)
            parameter("end", end)
            parameter("topNodesLimit", topNodesLimit)
        }.body()
}
