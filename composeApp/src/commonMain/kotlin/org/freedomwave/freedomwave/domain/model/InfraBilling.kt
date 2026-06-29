package org.freedomwave.domain.model

import org.freedomwave.data.api.dto.AvailableNodeDto
import org.freedomwave.data.api.dto.BillRecordDto
import org.freedomwave.data.api.dto.BillingNodeDto
import org.freedomwave.data.api.dto.BillingStatsDto
import org.freedomwave.data.api.dto.InfraProviderDto

data class BillingStats(
    val totalBillingNodes: Int,
    val upcomingCount: Int,
    val currentMonthPayments: Double,
    val totalSpent: Double,
) {
    companion object {
        fun from(totalBillingNodes: Int, dto: BillingStatsDto) = BillingStats(
            totalBillingNodes    = totalBillingNodes,
            upcomingCount        = dto.upcomingNodesCount,
            currentMonthPayments = dto.currentMonthPayments,
            totalSpent           = dto.totalSpent,
        )
    }
}

data class ProviderNodeRef(
    val nodeUuid: String,
    val name: String,
    val countryCode: String,
)

data class InfraProvider(
    val uuid: String,
    val name: String,
    val faviconLink: String?,
    val loginUrl: String?,
    val totalAmount: Double,
    val totalBills: Int,
    val nodes: List<ProviderNodeRef>,
) {
    companion object {
        fun from(dto: InfraProviderDto) = InfraProvider(
            uuid        = dto.uuid,
            name        = dto.name,
            faviconLink = dto.faviconLink,
            loginUrl    = dto.loginUrl,
            totalAmount = dto.billingHistory.totalAmount,
            totalBills  = dto.billingHistory.totalBills,
            nodes       = dto.billingNodes.map { ProviderNodeRef(it.nodeUuid, it.name, it.countryCode) },
        )
    }
}

data class BillingNode(
    val uuid: String,
    val nodeUuid: String,
    val providerUuid: String,
    val providerName: String,
    val nodeName: String,
    val countryCode: String,
    val nextBillingAt: String?,
) {
    companion object {
        fun from(dto: BillingNodeDto) = BillingNode(
            uuid          = dto.uuid,
            nodeUuid      = dto.nodeUuid,
            providerUuid  = dto.providerUuid,
            providerName  = dto.provider.name,
            nodeName      = dto.node.name,
            countryCode   = dto.node.countryCode,
            nextBillingAt = dto.nextBillingAt,
        )
    }
}

data class AvailableNode(
    val uuid: String,
    val name: String,
    val countryCode: String,
) {
    companion object {
        fun from(dto: AvailableNodeDto) = AvailableNode(dto.uuid, dto.name, dto.countryCode)
    }
}

data class BillRecord(
    val uuid: String,
    val providerUuid: String,
    val providerName: String,
    val amount: Double,
    val billedAt: String?,
) {
    companion object {
        fun from(dto: BillRecordDto) = BillRecord(
            uuid         = dto.uuid,
            providerUuid = dto.providerUuid,
            providerName = dto.provider.name,
            amount       = dto.amount,
            billedAt     = dto.billedAt,
        )
    }
}

/** Aggregate of the GET /nodes response: list + available pool + stats. */
data class BillingNodesBundle(
    val nodes: List<BillingNode>,
    val available: List<AvailableNode>,
    val stats: BillingStats,
)
