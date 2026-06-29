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

@Serializable
data class HwidDeviceDto(
    @SerialName("hwid")        val hwid: String,
    @SerialName("userUuid")    val userUuid: String,
    @SerialName("platform")    val platform: String? = null,
    @SerialName("osVersion")   val osVersion: String? = null,
    @SerialName("deviceModel") val deviceModel: String? = null,
    @SerialName("userAgent")   val userAgent: String? = null,
    @SerialName("createdAt")   val createdAt: String,
    @SerialName("updatedAt")   val updatedAt: String
)
