package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.SubscriptionTemplatesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class TemplateService(private val client: HttpClient) {
    suspend fun getTemplates(serverUrl: String): SubscriptionTemplatesResponse =
        client.get("$serverUrl/api/subscription-templates").body()
}
