package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.HwidDevicesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class HwidService(private val client: HttpClient) {

    suspend fun getDevices(serverUrl: String, userUuid: String): HwidDevicesResponse =
        client.get("$serverUrl/api/hwid/devices/$userUuid").body()
}
