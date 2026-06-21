package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.SubscriptionTemplatesResponse
import art.yniyniyni.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class TemplateService(private val client: HttpClient, private val prefs: AppPreferences) {
    suspend fun getTemplates(): SubscriptionTemplatesResponse =
        client.get("${prefs.getServerUrl()}/api/subscription-templates").body()
}
