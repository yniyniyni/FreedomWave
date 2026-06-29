package org.freedomwave.data.api.service

import org.freedomwave.data.api.dto.BillingHistoryResponse
import org.freedomwave.data.api.dto.BillingNodesResponse
import org.freedomwave.data.api.dto.CreateBillRecordRequest
import org.freedomwave.data.api.dto.CreateBillingNodeRequest
import org.freedomwave.data.api.dto.CreateInfraProviderRequest
import org.freedomwave.data.api.dto.InfraProviderResponse
import org.freedomwave.data.api.dto.InfraProvidersResponse
import org.freedomwave.data.api.dto.UpdateBillingNodeRequest
import org.freedomwave.data.api.dto.UpdateInfraProviderRequest
import org.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class InfraBillingService(private val client: HttpClient, private val prefs: AppPreferences) {

    // Providers
    suspend fun getProviders(): InfraProvidersResponse =
        client.get("${prefs.getServerUrl()}/api/infra-billing/providers").body()

    suspend fun createProvider(req: CreateInfraProviderRequest): InfraProviderResponse =
        client.post("${prefs.getServerUrl()}/api/infra-billing/providers") { setBody(req) }.body()

    suspend fun updateProvider(req: UpdateInfraProviderRequest): InfraProviderResponse =
        client.patch("${prefs.getServerUrl()}/api/infra-billing/providers") { setBody(req) }.body()

    suspend fun deleteProvider(uuid: String) {
        client.delete("${prefs.getServerUrl()}/api/infra-billing/providers/$uuid")
    }

    // Billing nodes
    suspend fun getBillingNodes(): BillingNodesResponse =
        client.get("${prefs.getServerUrl()}/api/infra-billing/nodes").body()

    suspend fun createBillingNode(req: CreateBillingNodeRequest): BillingNodesResponse =
        client.post("${prefs.getServerUrl()}/api/infra-billing/nodes") { setBody(req) }.body()

    suspend fun updateBillingNode(req: UpdateBillingNodeRequest): BillingNodesResponse =
        client.patch("${prefs.getServerUrl()}/api/infra-billing/nodes") { setBody(req) }.body()

    suspend fun deleteBillingNode(uuid: String) {
        client.delete("${prefs.getServerUrl()}/api/infra-billing/nodes/$uuid")
    }

    // Billing history
    suspend fun getHistory(start: Int = 0, size: Int = 100): BillingHistoryResponse =
        client.get("${prefs.getServerUrl()}/api/infra-billing/history") {
            parameter("start", start)
            parameter("size", size)
        }.body()

    suspend fun createHistory(req: CreateBillRecordRequest): BillingHistoryResponse =
        client.post("${prefs.getServerUrl()}/api/infra-billing/history") { setBody(req) }.body()

    suspend fun deleteHistory(uuid: String) {
        client.delete("${prefs.getServerUrl()}/api/infra-billing/history/$uuid")
    }
}
