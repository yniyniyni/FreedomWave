package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.ConfigProfilesResponse
import art.yniyniyni.freedomwave.data.api.dto.PubKeyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ConfigProfileService(private val client: HttpClient) {
    suspend fun getConfigProfiles(serverUrl: String): ConfigProfilesResponse =
        client.get("$serverUrl/api/config-profiles").body()

    suspend fun getPubKey(serverUrl: String): PubKeyResponse =
        client.get("$serverUrl/api/keygen").body()
}
