package org.freedomwave.ui.feature.nodes

import org.freedomwave.data.api.dto.CreateNodeRequest
import org.freedomwave.data.api.dto.NodeConfigProfileBody
import org.freedomwave.data.api.dto.UpdateNodeRequest

private const val BYTES_PER_GB = 1_073_741_824L
private val TAG_REGEX = Regex("^[A-Z0-9_:]+$")

/** Snapshot of the form's parsed values, shared by create/update builders (pure, testable). */
data class NodeFormInput(
    val name: String,
    val address: String,
    val port: Int?,
    val countryCode: String,
    val trackingActive: Boolean,
    val multiplier: Double?,
    val trafficLimitBytes: Long?,
    val resetDay: Int?,
    val notifyPercent: Int?,
    val tags: List<String>,
    val profileUuid: String,
    val inbounds: List<String>,
)

fun nameValid(s: String): Boolean = s.trim().length in 3..30
fun addressValid(s: String): Boolean = s.trim().length >= 2

fun portOrNull(s: String): Int? {
    if (s.isBlank()) return null
    val n = s.trim().toIntOrNull() ?: return null
    return if (n in 1..65535) n else null
}

fun gbToBytes(s: String): Long? {
    if (s.isBlank()) return null
    val gb = s.trim().replace(',', '.').toDoubleOrNull() ?: return null
    if (gb < 0) return null
    return (gb * BYTES_PER_GB).toLong()
}

fun multiplierOrNull(s: String): Double? {
    if (s.isBlank()) return null
    val d = s.trim().replace(',', '.').toDoubleOrNull() ?: return null
    return if (d in 0.0..100.0) d else null
}

fun resetDayOrNull(s: String): Int? {
    if (s.isBlank()) return null
    val n = s.trim().toIntOrNull() ?: return null
    return if (n in 1..31) n else null
}

fun notifyPercentOrNull(s: String): Int? {
    if (s.isBlank()) return null
    val n = s.trim().toIntOrNull() ?: return null
    return if (n in 0..100) n else null
}

fun parseTags(raw: String): List<String> =
    raw.split(',')
        .map { it.trim().uppercase() }
        .filter { it.isNotEmpty() && it.length <= 36 && TAG_REGEX.matches(it) }
        .take(10)

private fun NodeFormInput.body() = NodeConfigProfileBody(profileUuid, inbounds)

fun buildCreateRequest(input: NodeFormInput): CreateNodeRequest = CreateNodeRequest(
    name = input.name.trim(),
    address = input.address.trim(),
    port = input.port,
    countryCode = input.countryCode,
    isTrafficTrackingActive = input.trackingActive,
    trafficLimitBytes = if (input.trackingActive) input.trafficLimitBytes else null,
    trafficResetDay = if (input.trackingActive) input.resetDay else null,
    notifyPercent = if (input.trackingActive) input.notifyPercent else null,
    consumptionMultiplier = input.multiplier,
    tags = input.tags.ifEmpty { null },
    configProfile = input.body(),
)

fun buildUpdateRequest(uuid: String, input: NodeFormInput): UpdateNodeRequest = UpdateNodeRequest(
    uuid = uuid,
    name = input.name.trim(),
    address = input.address.trim(),
    port = input.port,
    countryCode = input.countryCode,
    isTrafficTrackingActive = input.trackingActive,
    trafficLimitBytes = if (input.trackingActive) input.trafficLimitBytes else null,
    trafficResetDay = if (input.trackingActive) input.resetDay else null,
    notifyPercent = if (input.trackingActive) input.notifyPercent else null,
    consumptionMultiplier = input.multiplier ?: 1.0,
    tags = input.tags,
    configProfile = input.body(),
)

/**
 * docker-compose.yml for a Remnawave node with the panel-issued SECRET_KEY.
 *
 * The env var is `SECRET_KEY` — `SSL_CERT` appears nowhere in the node or panel on either 2.8.x
 * or 3.x, so the snippet this used to emit produced a node that could not authenticate.
 */
fun buildNodeCompose(nodeSecretKey: String, port: Int?): String {
    val appPort = port ?: 2222
    return """
        services:
          remnawave-node:
            image: remnawave/node:latest
            container_name: remnawave-node
            hostname: remnawave-node
            restart: always
            network_mode: host
            environment:
              - APP_PORT=$appPort
              - SECRET_KEY=$nodeSecretKey
    """.trimIndent()
}
