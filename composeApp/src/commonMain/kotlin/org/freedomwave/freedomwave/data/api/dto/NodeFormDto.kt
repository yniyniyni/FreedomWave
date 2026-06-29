package org.freedomwave.data.api.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NodeConfigProfileBody(
    @SerialName("activeConfigProfileUuid") val activeConfigProfileUuid: String,
    @SerialName("activeInbounds")          val activeInbounds: List<String>,
)

/** POST /api/nodes. Optional fields default so encodeDefaults=false omits unset ones. */
@Serializable
data class CreateNodeRequest(
    @SerialName("name")                    val name: String,
    @SerialName("address")                 val address: String,
    @SerialName("port")                    val port: Int? = null,
    @SerialName("countryCode")             val countryCode: String = "XX",
    @SerialName("isTrafficTrackingActive") val isTrafficTrackingActive: Boolean = false,
    @SerialName("trafficLimitBytes")       val trafficLimitBytes: Long? = null,
    @SerialName("trafficResetDay")         val trafficResetDay: Int? = null,
    @SerialName("notifyPercent")           val notifyPercent: Int? = null,
    @SerialName("consumptionMultiplier")   val consumptionMultiplier: Double? = null,
    @SerialName("tags")                    val tags: List<String>? = null,
    @SerialName("configProfile")           val configProfile: NodeConfigProfileBody,
)

/** PATCH /api/nodes. Always-managed fields forced on with @EncodeDefault so e.g.
 *  turning tracking off (false == default) is still sent. port/limit/resetDay/
 *  notify stay nullable+default so they are omitted (not sent as null) when unset. */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
data class UpdateNodeRequest(
    @SerialName("uuid")                    val uuid: String,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("name")    val name: String,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("address") val address: String,
    @SerialName("port")                    val port: Int? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("countryCode")             val countryCode: String = "XX",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("isTrafficTrackingActive") val isTrafficTrackingActive: Boolean = false,
    @SerialName("trafficLimitBytes")       val trafficLimitBytes: Long? = null,
    @SerialName("trafficResetDay")         val trafficResetDay: Int? = null,
    @SerialName("notifyPercent")           val notifyPercent: Int? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("consumptionMultiplier")   val consumptionMultiplier: Double = 1.0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("tags")                    val tags: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("configProfile")           val configProfile: NodeConfigProfileBody,
)
