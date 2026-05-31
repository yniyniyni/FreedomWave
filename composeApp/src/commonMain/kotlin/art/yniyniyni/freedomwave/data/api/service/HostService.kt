package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.HostListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class HostService(private val client: HttpClient) {
    suspend fun getHosts(serverUrl: String): HostListResponse =
        client.get("$serverUrl/api/hosts").body()
}
