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
    val tags: List<String>,
    val isDisabled: Boolean,
    val isHidden: Boolean,
    val overrideSniFromAddress: Boolean,
    val keepSniBlank: Boolean,
    val pinnedPeerCertSha256: String?,
    val verifyPeerCertByName: String?,
    val mihomoIpVersion: String?,
    val vlessRouteId: Int?,
    val shuffleHost: Boolean,
    val mihomoX25519: Boolean,
    val nodes: List<String>,
    val xrayJsonTemplateUuid: String?,
)

fun remarkValid(s: String): Boolean = s.trim().length in 1..40
fun addressValid(s: String): Boolean = s.trim().isNotEmpty()

/**
 * True when every entry in the comma-separated tags field would survive [parseHostTags].
 *
 * Checked so the form can flag a bad tag instead of dropping it silently on save — the parse
 * is deliberately lenient, and without this the user would never learn a tag went missing.
 */
fun tagsValid(s: String): Boolean {
    val entries = s.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    if (entries.size > MAX_HOST_TAGS) return false
    return entries.all {
        it.length <= MAX_HOST_TAG_LENGTH && HOST_TAG_REGEX.matches(it.uppercase())
    }
}

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

/** Panel limit: at most 10 tags per host. */
const val MAX_HOST_TAGS = 10

/** Panel limit: each tag is at most 36 characters. */
const val MAX_HOST_TAG_LENGTH = 36

/**
 * Parse the comma-separated tags field into the list the panel accepts.
 *
 * Panel 2.8.1 replaced the single `tag` with a `tags` array. Entries are uppercased, then
 * anything the backend regex would reject is dropped rather than sent and 400'd — the field is
 * free text, so silently discarding a malformed entry keeps the rest of the save working.
 */
fun parseHostTags(raw: String): List<String> =
    raw.split(',')
        .map { it.trim().uppercase() }
        .filter { it.isNotEmpty() && it.length <= MAX_HOST_TAG_LENGTH && HOST_TAG_REGEX.matches(it) }
        .distinct()
        .take(MAX_HOST_TAGS)

/** Render stored tags back into the form's single comma-separated field. */
fun formatHostTags(tags: List<String>): String = tags.joinToString(", ")

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
    tags = input.tags,
    isHidden = input.isHidden,
    overrideSniFromAddress = input.overrideSniFromAddress,
    keepSniBlank = input.keepSniBlank,
    pinnedPeerCertSha256 = input.pinnedPeerCertSha256.orNullIfBlank(),
    verifyPeerCertByName = input.verifyPeerCertByName.orNullIfBlank(),
    mihomoIpVersion = input.mihomoIpVersion,
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
    tags = input.tags,
    isHidden = input.isHidden,
    overrideSniFromAddress = input.overrideSniFromAddress,
    keepSniBlank = input.keepSniBlank,
    pinnedPeerCertSha256 = input.pinnedPeerCertSha256.orNullIfBlank(),
    verifyPeerCertByName = input.verifyPeerCertByName.orNullIfBlank(),
    mihomoIpVersion = input.mihomoIpVersion,
    vlessRouteId = input.vlessRouteId,
    shuffleHost = input.shuffleHost,
    mihomoX25519 = input.mihomoX25519,
    nodes = input.nodes,
    xrayJsonTemplateUuid = input.xrayJsonTemplateUuid,
)
