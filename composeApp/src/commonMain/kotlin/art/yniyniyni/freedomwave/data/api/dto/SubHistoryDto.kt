package art.yniyniyni.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubHistoryResponse(
    @SerialName("response") val response: SubHistoryData
)

@Serializable
data class SubHistoryData(
    @SerialName("total")   val total: Int,
    @SerialName("records") val records: List<SubRequestRecordDto>
)

@Serializable
data class SubRequestRecordDto(
    @SerialName("id")        val id: Int,
    @SerialName("userUuid")  val userUuid: String,
    @SerialName("requestAt") val requestAt: String,
    @SerialName("requestIp") val requestIp: String? = null,
    @SerialName("userAgent") val userAgent: String? = null
)

/** Response from ipwho.is/{ip} */
@Serializable
data class IpWhoIsResponse(
    @SerialName("success")      val success: Boolean = false,
    @SerialName("ip")           val ip: String = "",
    @SerialName("city")         val city: String? = null,
    @SerialName("region")       val region: String? = null,
    @SerialName("country")      val country: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    @SerialName("connection")   val connection: IpConnectionDto? = null
)

@Serializable
data class IpConnectionDto(
    @SerialName("asn") val asn: Int? = null,
    @SerialName("isp") val isp: String? = null,
    @SerialName("org") val org: String? = null
)
