package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.BillingHistoryResponse
import art.yniyniyni.freedomwave.data.api.dto.BillingNodesResponse
import art.yniyniyni.freedomwave.data.api.dto.CreateBillRecordRequest
import art.yniyniyni.freedomwave.data.api.dto.CreateBillingNodeRequest
import art.yniyniyni.freedomwave.data.api.dto.CreateInfraProviderRequest
import art.yniyniyni.freedomwave.data.api.dto.InfraProviderResponse
import art.yniyniyni.freedomwave.data.api.dto.InfraProvidersResponse
import art.yniyniyni.freedomwave.data.api.dto.UpdateBillingNodeRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateInfraProviderRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class InfraBillingService(private val client: HttpClient) {

    // Providers
    suspend fun getProviders(serverUrl: String): InfraProvidersResponse =
        client.get("$serverUrl/api/infra-billing/providers").body()

    suspend fun createProvider(serverUrl: String, req: CreateInfraProviderRequest): InfraProviderResponse =
        client.post("$serverUrl/api/infra-billing/providers") { setBody(req) }.body()

    suspend fun updateProvider(serverUrl: String, req: UpdateInfraProviderRequest): InfraProviderResponse =
        client.patch("$serverUrl/api/infra-billing/providers") { setBody(req) }.body()

    suspend fun deleteProvider(serverUrl: String, uuid: String) {
        client.delete("$serverUrl/api/infra-billing/providers/$uuid")
    }

    // Billing nodes
    suspend fun getBillingNodes(serverUrl: String): BillingNodesResponse =
        client.get("$serverUrl/api/infra-billing/nodes").body()

    suspend fun createBillingNode(serverUrl: String, req: CreateBillingNodeRequest): BillingNodesResponse =
        client.post("$serverUrl/api/infra-billing/nodes") { setBody(req) }.body()

    suspend fun updateBillingNode(serverUrl: String, req: UpdateBillingNodeRequest): BillingNodesResponse =
        client.patch("$serverUrl/api/infra-billing/nodes") { setBody(req) }.body()

    suspend fun deleteBillingNode(serverUrl: String, uuid: String) {
        client.delete("$serverUrl/api/infra-billing/nodes/$uuid")
    }

    // Billing history
    suspend fun getHistory(serverUrl: String, start: Int = 0, size: Int = 100): BillingHistoryResponse =
        client.get("$serverUrl/api/infra-billing/history") {
            parameter("start", start)
            parameter("size", size)
        }.body()

    suspend fun createHistory(serverUrl: String, req: CreateBillRecordRequest): BillingHistoryResponse =
        client.post("$serverUrl/api/infra-billing/history") { setBody(req) }.body()

    suspend fun deleteHistory(serverUrl: String, uuid: String) {
        client.delete("$serverUrl/api/infra-billing/history/$uuid")
    }
}
