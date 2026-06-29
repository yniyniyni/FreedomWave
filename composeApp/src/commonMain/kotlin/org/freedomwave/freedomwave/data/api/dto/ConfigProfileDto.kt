package org.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConfigProfilesResponse(
    @SerialName("response") val response: ConfigProfilesData,
)

@Serializable
data class ConfigProfilesData(
    @SerialName("total")          val total: Int = 0,
    @SerialName("configProfiles") val configProfiles: List<ConfigProfileDto> = emptyList(),
)

@Serializable
data class ConfigProfileDto(
    @SerialName("uuid")     val uuid: String,
    @SerialName("name")     val name: String,
    @SerialName("inbounds") val inbounds: List<InboundDto> = emptyList(),
)

@Serializable
data class InboundDto(
    @SerialName("uuid")     val uuid: String,
    @SerialName("tag")      val tag: String,
    @SerialName("type")     val type: String,
    @SerialName("network")  val network: String? = null,
    @SerialName("security") val security: String? = null,
    @SerialName("port")     val port: Int? = null,
)

@Serializable
data class PubKeyResponse(
    @SerialName("response") val response: PubKeyData,
)

@Serializable
data class PubKeyData(
    @SerialName("pubKey") val pubKey: String,
)
