package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.ApiError
import art.yniyniyni.freedomwave.data.api.dto.CreateBillRecordRequest
import art.yniyniyni.freedomwave.data.api.dto.CreateBillingNodeRequest
import art.yniyniyni.freedomwave.data.api.dto.CreateInfraProviderRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateBillingNodeRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateInfraProviderRequest
import art.yniyniyni.freedomwave.data.api.service.InfraBillingService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.AvailableNode
import art.yniyniyni.freedomwave.domain.model.BillRecord
import art.yniyniyni.freedomwave.domain.model.BillingNode
import art.yniyniyni.freedomwave.domain.model.BillingNodesBundle
import art.yniyniyni.freedomwave.domain.model.BillingStats
import art.yniyniyni.freedomwave.domain.model.InfraProvider

class InfraBillingRepository(
    private val service: InfraBillingService,
    private val prefs: AppPreferences,
) {
    // Providers
    suspend fun getProviders(): Result<List<InfraProvider>> = runCatching {
        service.getProviders(prefs.getServerUrl()).response.providers.map { InfraProvider.from(it) }
    }.also { clearOnUnauthorized(it) }

    suspend fun createProvider(name: String, faviconLink: String?, loginUrl: String?): Result<Unit> = runCatching {
        service.createProvider(prefs.getServerUrl(), CreateInfraProviderRequest(name, faviconLink, loginUrl)); Unit
    }.also { clearOnUnauthorized(it) }

    suspend fun updateProvider(uuid: String, name: String, faviconLink: String?, loginUrl: String?): Result<Unit> = runCatching {
        service.updateProvider(prefs.getServerUrl(), UpdateInfraProviderRequest(uuid, name, faviconLink, loginUrl)); Unit
    }.also { clearOnUnauthorized(it) }

    suspend fun deleteProvider(uuid: String): Result<Unit> = runCatching {
        service.deleteProvider(prefs.getServerUrl(), uuid)
    }.also { clearOnUnauthorized(it) }

    // Billing nodes
    suspend fun getBillingNodes(): Result<BillingNodesBundle> = runCatching {
        val data = service.getBillingNodes(prefs.getServerUrl()).response
        BillingNodesBundle(
            nodes     = data.billingNodes.map { BillingNode.from(it) },
            available = data.availableBillingNodes.map { AvailableNode.from(it) },
            stats     = BillingStats.from(data.totalBillingNodes, data.stats),
        )
    }.also { clearOnUnauthorized(it) }

    suspend fun createBillingNode(providerUuid: String, nodeUuid: String, nextBillingAt: String?): Result<Unit> = runCatching {
        service.createBillingNode(prefs.getServerUrl(), CreateBillingNodeRequest(providerUuid, nodeUuid, nextBillingAt)); Unit
    }.also { clearOnUnauthorized(it) }

    suspend fun updateBillingNodeDate(uuid: String, nextBillingAt: String): Result<Unit> = runCatching {
        service.updateBillingNode(prefs.getServerUrl(), UpdateBillingNodeRequest(listOf(uuid), nextBillingAt)); Unit
    }.also { clearOnUnauthorized(it) }

    suspend fun deleteBillingNode(uuid: String): Result<Unit> = runCatching {
        service.deleteBillingNode(prefs.getServerUrl(), uuid)
    }.also { clearOnUnauthorized(it) }

    // Billing history
    suspend fun getHistory(): Result<List<BillRecord>> = runCatching {
        service.getHistory(prefs.getServerUrl()).response.records.map { BillRecord.from(it) }
    }.also { clearOnUnauthorized(it) }

    suspend fun createHistory(providerUuid: String, amount: Double, billedAt: String): Result<Unit> = runCatching {
        service.createHistory(prefs.getServerUrl(), CreateBillRecordRequest(providerUuid, amount, billedAt)); Unit
    }.also { clearOnUnauthorized(it) }

    suspend fun deleteHistory(uuid: String): Result<Unit> = runCatching {
        service.deleteHistory(prefs.getServerUrl(), uuid)
    }.also { clearOnUnauthorized(it) }

    private suspend fun <T> clearOnUnauthorized(result: Result<T>) {
        if (result.exceptionOrNull() is ApiError.Unauthorized) prefs.clearCredentials()
    }
}
