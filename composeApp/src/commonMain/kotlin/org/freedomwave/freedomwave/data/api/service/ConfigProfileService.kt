package org.freedomwave.data.api.service

import org.freedomwave.data.api.dto.ConfigProfilesResponse
import org.freedomwave.data.api.dto.PubKeyResponse
import org.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ConfigProfileService(private val client: HttpClient, private val prefs: AppPreferences) {
    suspend fun getConfigProfiles(): ConfigProfilesResponse =
        client.get("${prefs.getServerUrl()}/api/config-profiles").body()

    suspend fun getPubKey(): PubKeyResponse =
        client.get("${prefs.getServerUrl()}/api/keygen").body()
}
