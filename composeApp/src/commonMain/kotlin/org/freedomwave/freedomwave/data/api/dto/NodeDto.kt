package org.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NodeListResponse(
    @SerialName("response") val response: List<NodeDto>
)

@Serializable
data class NodeResponse(
    @SerialName("response") val response: NodeDto
)

@Serializable
data class NodeDto(
    @SerialName("uuid")                     val uuid: String,
    // Added in panel 3.1.0; absent on 2.8.x. Node routes are still keyed by uuid on both, so
    // this is informational only — unlike users, nodes did not switch identifier.
    @SerialName("id")                       val id: Int? = null,
    @SerialName("name")                     val name: String,
    @SerialName("address")                  val address: String,
    @SerialName("port")                     val port: Int? = null,
    @SerialName("isConnected")              val isConnected: Boolean,
    @SerialName("isDisabled")              val isDisabled: Boolean,
    @SerialName("isConnecting")             val isConnecting: Boolean,
    @SerialName("lastStatusChange")         val lastStatusChange: String? = null,
    @SerialName("lastStatusMessage")        val lastStatusMessage: String? = null,
    @SerialName("isTrafficTrackingActive")  val isTrafficTrackingActive: Boolean,
    @SerialName("trafficLimitBytes")        val trafficLimitBytes: Long? = null,
    @SerialName("trafficUsedBytes")         val trafficUsedBytes: Long? = null,
    @SerialName("trafficResetDay")          val trafficResetDay: Int? = null,
    @SerialName("consumptionMultiplier")    val consumptionMultiplier: Double = 1.0,
    @SerialName("notifyPercent")            val notifyPercent: Int? = null,
    @SerialName("xrayUptime")               val xrayUptime: Double = 0.0,
    @SerialName("usersOnline")              val usersOnline: Int = 0,
    @SerialName("versions")                 val versions: NodeVersionsDto? = null,
    @SerialName("configProfile")            val configProfile: NodeConfigProfileRef? = null,
    @SerialName("viewPosition")             val viewPosition: Int,
    @SerialName("countryCode")              val countryCode: String,
    @SerialName("tags")                     val tags: List<String> = emptyList(),
    // Added in panel 3.x — the node's assigned addresses and what each is used for.
    @SerialName("ips")                      val ips: List<NodeIpDto> = emptyList(),
    @SerialName("system")                   val system: NodeSystemDto? = null,
    @SerialName("createdAt")               val createdAt: String,
    @SerialName("updatedAt")               val updatedAt: String
)

/**
 * One address assigned to a node, with what the panel uses it for.
 *
 * `status` is a `NODE_IP_STATUSES` value — INBOUND, OUTBOUND, MANAGEMENT, TRANSIT, MONITORING,
 * RESERVE, BLOCKED, FLAGGED, DEPRECATED or UNKNOWN. Kept as a String rather than an enum so a
 * status added by a future panel does not fail the whole node list.
 */
@Serializable
data class NodeIpDto(
    @SerialName("ip")     val ip: String,
    @SerialName("status") val status: String,
)

@Serializable
data class NodeSystemDto(
    @SerialName("info")  val info: NodeSystemInfoDto,
    @SerialName("stats") val stats: NodeSystemStatsDto
)

@Serializable
data class NodeSystemInfoDto(
    @SerialName("arch")       val arch: String,
    @SerialName("cpus")       val cpus: Int,
    @SerialName("cpuModel")   val cpuModel: String,
    @SerialName("memoryTotal") val memoryTotal: Long,
    @SerialName("hostname")   val hostname: String,
    @SerialName("platform")   val platform: String
)

@Serializable
data class NodeSystemStatsDto(
    @SerialName("memoryFree") val memoryFree: Long,
    @SerialName("memoryUsed") val memoryUsed: Long,
    @SerialName("uptime")     val uptime: Double,
    @SerialName("loadAvg")    val loadAvg: List<Double> = emptyList()
)

@Serializable
data class NodeVersionsDto(
    @SerialName("xray") val xray: String,
    @SerialName("node") val node: String
)

@Serializable
data class NodeConfigProfileRef(
    @SerialName("activeConfigProfileUuid") val activeConfigProfileUuid: String? = null,
    // The node response returns activeInbounds as an array of inbound OBJECTS
    // (not UUID strings — that's the create/update request shape). Parse as objects.
    @SerialName("activeInbounds")          val activeInbounds: List<InboundDto> = emptyList(),
)

@Serializable
data class ReorderNodeItem(
    @SerialName("uuid")         val uuid: String,
    @SerialName("viewPosition") val viewPosition: Int,
)

@Serializable
data class ReorderNodesRequest(
    @SerialName("nodes") val nodes: List<ReorderNodeItem>,
)

fun reorderNodesPayload(orderedUuids: List<String>): List<ReorderNodeItem> =
    orderedUuids.mapIndexed { index, uuid -> ReorderNodeItem(uuid = uuid, viewPosition = index) }
