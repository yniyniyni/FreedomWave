package org.freedomwave.data.api.dto

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

/**
 * One subscription request. Panel 3.x identifies the user by numeric [userId]; 2.8.x sent
 * [userUuid]. Both are nullable so one build decodes either — a non-nullable `userUuid` here
 * is what made the IP addresses section fail against 3.x.
 */
@Serializable
data class SubRequestRecordDto(
    @SerialName("id")              val id: Int,
    @SerialName("userId")          val userId: Int? = null,
    @SerialName("userUuid")        val userUuid: String? = null,
    @SerialName("requestAt")       val requestAt: String,
    @SerialName("requestIp")       val requestIp: String? = null,
    @SerialName("userAgent")       val userAgent: String? = null,
    // Added in panel 3.1.0 — which response rule matched, and what it returned.
    @SerialName("srrResponseType") val srrResponseType: String? = null,
    @SerialName("srrRuleName")     val srrRuleName: String? = null
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
