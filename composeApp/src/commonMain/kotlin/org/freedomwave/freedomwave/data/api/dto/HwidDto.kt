package org.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HwidDevicesResponse(
    @SerialName("response") val response: HwidDevicesData
)

@Serializable
data class HwidDevicesData(
    @SerialName("total")   val total: Int,
    @SerialName("devices") val devices: List<HwidDeviceDto>
)

/**
 * A registered device. Panel 3.x identifies the owner by numeric [userId]; 2.8.x sent
 * [userUuid]. Both are nullable so one build decodes either — a non-nullable `userUuid` here
 * is what made the Devices section fail against 3.x.
 */
@Serializable
data class HwidDeviceDto(
    @SerialName("hwid")        val hwid: String,
    @SerialName("userId")      val userId: Int? = null,
    @SerialName("userUuid")    val userUuid: String? = null,
    @SerialName("platform")    val platform: String? = null,
    @SerialName("osVersion")   val osVersion: String? = null,
    @SerialName("deviceModel") val deviceModel: String? = null,
    @SerialName("userAgent")   val userAgent: String? = null,
    // Added in 2.8.1 alongside optional per-device IP tracking.
    @SerialName("requestIp")   val requestIp: String? = null,
    @SerialName("createdAt")   val createdAt: String,
    @SerialName("updatedAt")   val updatedAt: String
) {
    /** Owner identity in whichever form this panel sent it. */
    val ownerRef: String get() = userUuid ?: userId?.toString().orEmpty()
}
