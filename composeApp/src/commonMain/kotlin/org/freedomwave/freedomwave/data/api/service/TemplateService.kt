package org.freedomwave.data.api.service

import org.freedomwave.data.api.dto.SubscriptionTemplatesResponse
import org.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class TemplateService(private val client: HttpClient, private val prefs: AppPreferences) {
    suspend fun getTemplates(): SubscriptionTemplatesResponse =
        client.get("${prefs.getServerUrl()}/api/subscription-templates").body()
}
