package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.SubPageConfigsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class SubPageConfigService(private val client: HttpClient) {
    suspend fun getConfigs(serverUrl: String): SubPageConfigsResponse =
        client.get("$serverUrl/api/subscription-page-configs").body()
}
