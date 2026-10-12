package org.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /api/system/nodes/metrics` — per-node Xray inbound/outbound counters. */
@Serializable
data class NodesMetricsResponse(
    @SerialName("response") val response: NodesMetricsData
)

@Serializable
data class NodesMetricsData(
    @SerialName("nodes") val nodes: List<NodeMetricsDto>
)

@Serializable
data class NodeMetricsDto(
    @SerialName("nodeUuid")       val nodeUuid: String,
    @SerialName("nodeName")       val nodeName: String,
    @SerialName("countryEmoji")   val countryEmoji: String = "",
    @SerialName("providerName")   val providerName: String = "",
    @SerialName("usersOnline")    val usersOnline: Int = 0,
    @SerialName("inboundsStats")  val inboundsStats: List<TagTrafficDto> = emptyList(),
    @SerialName("outboundsStats") val outboundsStats: List<TagTrafficDto> = emptyList()
)

/** Upload/download arrive pre-formatted by the panel, e.g. `"45.62 GiB"` or `"0"`. */
@Serializable
data class TagTrafficDto(
    @SerialName("tag")      val tag: String,
    @SerialName("upload")   val upload: String,
    @SerialName("download") val download: String
)
