package org.freedomwave.data.api.service

import org.freedomwave.data.api.dto.HwidDevicesResponse
import org.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class HwidService(private val client: HttpClient, private val prefs: AppPreferences) {

    suspend fun getDevices(userRef: String): HwidDevicesResponse =
        client.get("${prefs.getServerUrl()}/api/hwid/devices/$userRef").body()
}
