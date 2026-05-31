package art.yniyniyni.freedomwave.data.api.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get

class AuthService(private val client: HttpClient) {
    suspend fun verifyConnection(serverUrl: String) {
        client.get("$serverUrl/api/system/stats/recap")
    }
}
