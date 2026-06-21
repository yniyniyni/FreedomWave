package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.HwidDevicesResponse
import art.yniyniyni.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class HwidService(private val client: HttpClient, private val prefs: AppPreferences) {

    suspend fun getDevices(userUuid: String): HwidDevicesResponse =
        client.get("${prefs.getServerUrl()}/api/hwid/devices/$userUuid").body()
}
