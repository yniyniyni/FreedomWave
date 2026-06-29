package org.freedomwave.data.api.service

import org.freedomwave.data.api.dto.SubPageConfigsResponse
import org.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class SubPageConfigService(private val client: HttpClient, private val prefs: AppPreferences) {
    suspend fun getConfigs(): SubPageConfigsResponse =
        client.get("${prefs.getServerUrl()}/api/subscription-page-configs").body()
}
