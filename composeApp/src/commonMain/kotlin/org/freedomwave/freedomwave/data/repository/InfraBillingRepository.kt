package org.freedomwave.data.repository

import org.freedomwave.data.api.dto.CreateBillRecordRequest
import org.freedomwave.data.api.dto.CreateBillingNodeRequest
import org.freedomwave.data.api.dto.CreateInfraProviderRequest
import org.freedomwave.data.api.dto.UpdateBillingNodeRequest
import org.freedomwave.data.api.dto.UpdateInfraProviderRequest
import org.freedomwave.data.api.service.InfraBillingService
import org.freedomwave.data.store.AppPreferences
import org.freedomwave.domain.model.AvailableNode
import org.freedomwave.domain.model.BillRecord
import org.freedomwave.domain.model.BillingNode
import org.freedomwave.domain.model.BillingNodesBundle
import org.freedomwave.domain.model.BillingStats
import org.freedomwave.domain.model.InfraProvider

class InfraBillingRepository(
    private val service: InfraBillingService,
    private val prefs: AppPreferences,
) {
    suspend fun getProviders(): Result<List<InfraProvider>> = runCatching {
        service.getProviders().response.providers.map { InfraProvider.from(it) }
    }.clearOnUnauthorized(prefs)

    suspend fun createProvider(name: String, faviconLink: String?, loginUrl: String?): Result<Unit> = runCatching {
        service.createProvider(CreateInfraProviderRequest(name, faviconLink, loginUrl))
        Unit
    }.clearOnUnauthorized(prefs)

    suspend fun updateProvider(uuid: String, name: String, faviconLink: String?, loginUrl: String?): Result<Unit> = runCatching {
        service.updateProvider(UpdateInfraProviderRequest(uuid, name, faviconLink, loginUrl))
        Unit
    }.clearOnUnauthorized(prefs)

    suspend fun deleteProvider(uuid: String): Result<Unit> = runCatching {
        service.deleteProvider(uuid)
    }.clearOnUnauthorized(prefs)

    suspend fun getBillingNodes(): Result<BillingNodesBundle> = runCatching {
        val data = service.getBillingNodes().response
        BillingNodesBundle(
            nodes     = data.billingNodes.map { BillingNode.from(it) },
            available = data.availableBillingNodes.map { AvailableNode.from(it) },
            stats     = BillingStats.from(data.totalBillingNodes, data.stats),
        )
    }.clearOnUnauthorized(prefs)

    suspend fun createBillingNode(providerUuid: String, nodeUuid: String, nextBillingAt: String?): Result<Unit> = runCatching {
        service.createBillingNode(CreateBillingNodeRequest(providerUuid, nodeUuid, nextBillingAt))
        Unit
    }.clearOnUnauthorized(prefs)

    suspend fun updateBillingNodeDate(uuid: String, nextBillingAt: String): Result<Unit> = runCatching {
        service.updateBillingNode(UpdateBillingNodeRequest(listOf(uuid), nextBillingAt))
        Unit
    }.clearOnUnauthorized(prefs)

    suspend fun deleteBillingNode(uuid: String): Result<Unit> = runCatching {
        service.deleteBillingNode(uuid)
    }.clearOnUnauthorized(prefs)

    suspend fun getHistory(): Result<List<BillRecord>> = runCatching {
        service.getHistory().response.records.map { BillRecord.from(it) }
    }.clearOnUnauthorized(prefs)

    suspend fun createHistory(providerUuid: String, amount: Double, billedAt: String): Result<Unit> = runCatching {
        service.createHistory(CreateBillRecordRequest(providerUuid, amount, billedAt))
        Unit
    }.clearOnUnauthorized(prefs)

    suspend fun deleteHistory(uuid: String): Result<Unit> = runCatching {
        service.deleteHistory(uuid)
    }.clearOnUnauthorized(prefs)
}
