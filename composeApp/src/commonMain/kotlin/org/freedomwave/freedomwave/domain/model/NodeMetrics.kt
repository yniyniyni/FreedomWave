package org.freedomwave.domain.model

import org.freedomwave.data.api.dto.NodeMetricsDto
import org.freedomwave.data.api.dto.TagTrafficDto
import org.freedomwave.util.parseHumanBytes

/** Traffic through one Xray inbound/outbound tag. Bytes are null if the panel's string didn't parse. */
data class TagTraffic(
    val tag: String,
    val uploadBytes: Long?,
    val downloadBytes: Long?,
    val uploadRaw: String,
    val downloadRaw: String,
)

data class NodeMetrics(
    val nodeUuid: String,
    val nodeName: String,
    val countryEmoji: String,
    val providerName: String,
    val usersOnline: Int,
    val inbounds: List<TagTraffic>,
    val outbounds: List<TagTraffic>,
) {
    companion object {
        fun from(dto: NodeMetricsDto) = NodeMetrics(
            nodeUuid     = dto.nodeUuid,
            nodeName     = dto.nodeName,
            countryEmoji = dto.countryEmoji,
            providerName = dto.providerName,
            usersOnline  = dto.usersOnline,
            inbounds     = dto.inboundsStats.map { it.toDomain() },
            outbounds    = dto.outboundsStats.map { it.toDomain() },
        )

        private fun TagTrafficDto.toDomain() = TagTraffic(
            tag           = tag,
            uploadBytes   = parseHumanBytes(upload),
            downloadBytes = parseHumanBytes(download),
            uploadRaw     = upload,
            downloadRaw   = download,
        )
    }
}
