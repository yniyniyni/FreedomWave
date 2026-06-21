package art.yniyniyni.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Providers

@Serializable
data class InfraProvidersResponse(
    @SerialName("response") val response: InfraProvidersData
)

@Serializable
data class InfraProvidersData(
    @SerialName("total")     val total: Int,
    @SerialName("providers") val providers: List<InfraProviderDto> = emptyList()
)

@Serializable
data class InfraProviderResponse(
    @SerialName("response") val response: InfraProviderDto
)

@Serializable
data class InfraProviderDto(
    @SerialName("uuid")           val uuid: String,
    @SerialName("name")           val name: String,
    @SerialName("faviconLink")    val faviconLink: String? = null,
    @SerialName("loginUrl")       val loginUrl: String? = null,
    @SerialName("createdAt")      val createdAt: String? = null,
    @SerialName("updatedAt")      val updatedAt: String? = null,
    @SerialName("billingHistory") val billingHistory: ProviderBillingHistoryDto = ProviderBillingHistoryDto(),
    @SerialName("billingNodes")   val billingNodes: List<ProviderNodeRefDto> = emptyList()
)

@Serializable
data class ProviderBillingHistoryDto(
    @SerialName("totalAmount") val totalAmount: Double = 0.0,
    @SerialName("totalBills")  val totalBills: Int = 0
)

@Serializable
data class ProviderNodeRefDto(
    @SerialName("nodeUuid")    val nodeUuid: String,
    @SerialName("name")        val name: String,
    @SerialName("countryCode") val countryCode: String = ""
)

// Billing nodes

@Serializable
data class BillingNodesResponse(
    @SerialName("response") val response: BillingNodesData
)

@Serializable
data class BillingNodesData(
    @SerialName("totalBillingNodes")          val totalBillingNodes: Int = 0,
    @SerialName("billingNodes")               val billingNodes: List<BillingNodeDto> = emptyList(),
    @SerialName("availableBillingNodes")      val availableBillingNodes: List<AvailableNodeDto> = emptyList(),
    @SerialName("totalAvailableBillingNodes") val totalAvailableBillingNodes: Int = 0,
    @SerialName("stats")                      val stats: BillingStatsDto = BillingStatsDto()
)

@Serializable
data class BillingStatsDto(
    @SerialName("upcomingNodesCount")    val upcomingNodesCount: Int = 0,
    @SerialName("currentMonthPayments")  val currentMonthPayments: Double = 0.0,
    @SerialName("totalSpent")            val totalSpent: Double = 0.0
)

@Serializable
data class BillingNodeDto(
    @SerialName("uuid")          val uuid: String,
    @SerialName("nodeUuid")      val nodeUuid: String,
    @SerialName("providerUuid")  val providerUuid: String,
    @SerialName("provider")      val provider: BillingNodeProviderDto,
    @SerialName("node")          val node: BillingNodeNodeDto,
    @SerialName("nextBillingAt") val nextBillingAt: String? = null,
    @SerialName("createdAt")     val createdAt: String? = null,
    @SerialName("updatedAt")     val updatedAt: String? = null
)

@Serializable
data class BillingNodeProviderDto(
    @SerialName("uuid")        val uuid: String,
    @SerialName("name")        val name: String,
    @SerialName("loginUrl")    val loginUrl: String? = null,
    @SerialName("faviconLink") val faviconLink: String? = null
)

@Serializable
data class BillingNodeNodeDto(
    @SerialName("uuid")        val uuid: String,
    @SerialName("name")        val name: String,
    @SerialName("countryCode") val countryCode: String = ""
)

@Serializable
data class AvailableNodeDto(
    @SerialName("uuid")        val uuid: String,
    @SerialName("name")        val name: String,
    @SerialName("countryCode") val countryCode: String = ""
)

// Billing history

@Serializable
data class BillingHistoryResponse(
    @SerialName("response") val response: BillingHistoryData
)

@Serializable
data class BillingHistoryData(
    @SerialName("records") val records: List<BillRecordDto> = emptyList(),
    @SerialName("total")   val total: Int = 0
)

@Serializable
data class BillRecordDto(
    @SerialName("uuid")         val uuid: String,
    @SerialName("providerUuid") val providerUuid: String,
    @SerialName("amount")       val amount: Double,
    @SerialName("billedAt")     val billedAt: String? = null,
    @SerialName("provider")     val provider: BillRecordProviderDto
)

@Serializable
data class BillRecordProviderDto(
    @SerialName("uuid")        val uuid: String,
    @SerialName("name")        val name: String,
    @SerialName("faviconLink") val faviconLink: String? = null
)

// Requests

@Serializable
data class CreateInfraProviderRequest(
    @SerialName("name")        val name: String,
    @SerialName("faviconLink") val faviconLink: String? = null,
    @SerialName("loginUrl")    val loginUrl: String? = null
)

@Serializable
data class UpdateInfraProviderRequest(
    @SerialName("uuid")        val uuid: String,
    @SerialName("name")        val name: String? = null,
    @SerialName("faviconLink") val faviconLink: String? = null,
    @SerialName("loginUrl")    val loginUrl: String? = null
)

@Serializable
data class CreateBillingNodeRequest(
    @SerialName("providerUuid")  val providerUuid: String,
    @SerialName("nodeUuid")      val nodeUuid: String,
    @SerialName("nextBillingAt") val nextBillingAt: String? = null
)

@Serializable
data class UpdateBillingNodeRequest(
    @SerialName("uuids")         val uuids: List<String>,
    @SerialName("nextBillingAt") val nextBillingAt: String
)

@Serializable
data class CreateBillRecordRequest(
    @SerialName("providerUuid") val providerUuid: String,
    @SerialName("amount")       val amount: Double,
    @SerialName("billedAt")     val billedAt: String
)
