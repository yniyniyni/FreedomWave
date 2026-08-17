package org.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HostListResponse(
    @SerialName("response") val response: List<HostDto>
)

@Serializable
data class HostResponse(
    @SerialName("response") val response: HostDto
)

@Serializable
data class HostInboundRef(
    @SerialName("configProfileUuid")        val configProfileUuid: String? = null,
    @SerialName("configProfileInboundUuid") val configProfileInboundUuid: String? = null,
)

@Serializable
data class HostDto(
    @SerialName("uuid")                  val uuid: String,
    @SerialName("remark")                val remark: String,
    @SerialName("address")               val address: String,
    @SerialName("port")                  val port: Int,
    @SerialName("path")                  val path: String? = null,
    @SerialName("sni")                   val sni: String? = null,
    @SerialName("host")                  val host: String? = null,
    @SerialName("alpn")                  val alpn: String? = null,
    @SerialName("fingerprint")           val fingerprint: String? = null,
    @SerialName("isDisabled")            val isDisabled: Boolean = false,
    @SerialName("isHidden")              val isHidden: Boolean = false,
    @SerialName("securityLayer")         val securityLayer: String = "DEFAULT",
    @SerialName("tags")                  val tags: List<String> = emptyList(),
    @SerialName("serverDescription")     val serverDescription: String? = null,
    @SerialName("pinnedPeerCertSha256")  val pinnedPeerCertSha256: String? = null,
    @SerialName("verifyPeerCertByName")  val verifyPeerCertByName: String? = null,
    @SerialName("mihomoIpVersion")       val mihomoIpVersion: String? = null,
    @SerialName("overrideSniFromAddress") val overrideSniFromAddress: Boolean = false,
    @SerialName("keepSniBlank")          val keepSniBlank: Boolean = false,
    @SerialName("shuffleHost")           val shuffleHost: Boolean = false,
    @SerialName("mihomoX25519")          val mihomoX25519: Boolean = false,
    @SerialName("viewPosition")          val viewPosition: Int = 0,
    @SerialName("nodes")                 val nodes: List<String> = emptyList(),
    @SerialName("inbound")               val inbound: HostInboundRef? = null,
    @SerialName("vlessRouteId")          val vlessRouteId: Int? = null,
    @SerialName("xrayJsonTemplateUuid")  val xrayJsonTemplateUuid: String? = null,
)

@Serializable
data class BulkUuidsRequest(
    @SerialName("uuids") val uuids: List<String>
)

@Serializable
data class ReorderHostItem(
    @SerialName("uuid")         val uuid: String,
    @SerialName("viewPosition") val viewPosition: Int,
)

@Serializable
data class ReorderHostsRequest(
    @SerialName("hosts") val hosts: List<ReorderHostItem>,
)

fun reorderHostsPayload(orderedUuids: List<String>): List<ReorderHostItem> =
    orderedUuids.mapIndexed { index, uuid -> ReorderHostItem(uuid = uuid, viewPosition = index) }
