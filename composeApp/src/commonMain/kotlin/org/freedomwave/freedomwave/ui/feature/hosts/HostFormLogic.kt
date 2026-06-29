package org.freedomwave.ui.feature.hosts

import org.freedomwave.data.api.dto.CreateHostRequest
import org.freedomwave.data.api.dto.HostInboundBody
import org.freedomwave.data.api.dto.UpdateHostRequest

private val HOST_TAG_REGEX = Regex("^[A-Z0-9_:]+$")
private const val MAX_SERVER_DESC_LENGTH = 30

data class HostFormInput(
    val configProfileUuid: String,
    val configProfileInboundUuid: String,
    val remark: String,
    val address: String,
    val port: Int,
    val path: String?,
    val sni: String?,
    val host: String?,
    val alpn: String?,
    val fingerprint: String?,
    val securityLayer: String,
    val serverDescription: String?,
    val tag: String?,
    val isDisabled: Boolean,
    val isHidden: Boolean,
    val overrideSniFromAddress: Boolean,
    val keepSniBlank: Boolean,
    val allowInsecure: Boolean,
    val vlessRouteId: Int?,
    val shuffleHost: Boolean,
    val mihomoX25519: Boolean,
    val nodes: List<String>,
    val xrayJsonTemplateUuid: String?,
)

fun remarkValid(s: String): Boolean = s.trim().length in 1..40
fun addressValid(s: String): Boolean = s.trim().isNotEmpty()

fun tagValid(s: String): Boolean = s.isEmpty() || parseHostTag(s) != null

fun vlessRouteIdValid(s: String): Boolean {
    if (s.isBlank()) return true // optional field, empty is valid
    return vlessRouteIdOrNull(s) != null
}

fun serverDescriptionValid(s: String): Boolean = s.trim().length <= MAX_SERVER_DESC_LENGTH

fun portOrNull(s: String): Int? {
    val n = s.trim().toIntOrNull() ?: return null
    return if (n in 1..65535) n else null
}

fun vlessRouteIdOrNull(s: String): Int? {
    if (s.isBlank()) return null
    val n = s.trim().toIntOrNull() ?: return null
    return if (n in 0..65535) n else null
}

fun serverDescriptionOrNull(s: String): String? =
    s.trim().take(MAX_SERVER_DESC_LENGTH).ifBlank { null }

fun parseHostTag(raw: String): String? {
    val t = raw.trim().uppercase()
    return if (t.isNotEmpty() && t.length <= 32 && HOST_TAG_REGEX.matches(t)) t else null
}

private fun HostFormInput.inboundBody() =
    HostInboundBody(configProfileUuid, configProfileInboundUuid)

private fun String?.orNullIfBlank(): String? = this?.trim()?.ifBlank { null }

fun buildCreateRequest(input: HostFormInput): CreateHostRequest = CreateHostRequest(
    inbound = input.inboundBody(),
    remark = input.remark.trim(),
    address = input.address.trim(),
    port = input.port,
    path = input.path.orNullIfBlank(),
    sni = input.sni.orNullIfBlank(),
    host = input.host.orNullIfBlank(),
    alpn = input.alpn,
    fingerprint = input.fingerprint,
    isDisabled = input.isDisabled,
    securityLayer = input.securityLayer,
    serverDescription = input.serverDescription.orNullIfBlank(),
    tag = input.tag,
    isHidden = input.isHidden,
    overrideSniFromAddress = input.overrideSniFromAddress,
    keepSniBlank = input.keepSniBlank,
    allowInsecure = input.allowInsecure,
    vlessRouteId = input.vlessRouteId,
    shuffleHost = input.shuffleHost,
    mihomoX25519 = input.mihomoX25519,
    nodes = input.nodes.ifEmpty { null },
    xrayJsonTemplateUuid = input.xrayJsonTemplateUuid,
)

fun buildUpdateRequest(uuid: String, input: HostFormInput): UpdateHostRequest = UpdateHostRequest(
    uuid = uuid,
    inbound = input.inboundBody(),
    remark = input.remark.trim(),
    address = input.address.trim(),
    port = input.port,
    path = input.path.orNullIfBlank(),
    sni = input.sni.orNullIfBlank(),
    host = input.host.orNullIfBlank(),
    alpn = input.alpn,
    fingerprint = input.fingerprint,
    isDisabled = input.isDisabled,
    securityLayer = input.securityLayer,
    serverDescription = input.serverDescription.orNullIfBlank(),
    tag = input.tag,
    isHidden = input.isHidden,
    overrideSniFromAddress = input.overrideSniFromAddress,
    keepSniBlank = input.keepSniBlank,
    allowInsecure = input.allowInsecure,
    vlessRouteId = input.vlessRouteId,
    shuffleHost = input.shuffleHost,
    mihomoX25519 = input.mihomoX25519,
    nodes = input.nodes,
    xrayJsonTemplateUuid = input.xrayJsonTemplateUuid,
)
