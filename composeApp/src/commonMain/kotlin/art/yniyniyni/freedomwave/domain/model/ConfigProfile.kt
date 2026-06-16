package art.yniyniyni.freedomwave.domain.model

import art.yniyniyni.freedomwave.data.api.dto.ConfigProfileDto

data class Inbound(
    val uuid: String,
    val tag: String,
    val type: String,
    val network: String?,
    val security: String?,
    val port: Int?,
)

data class ConfigProfile(
    val uuid: String,
    val name: String,
    val inbounds: List<Inbound>,
) {
    companion object {
        fun from(dto: ConfigProfileDto) = ConfigProfile(
            uuid = dto.uuid,
            name = dto.name,
            inbounds = dto.inbounds.map {
                Inbound(it.uuid, it.tag, it.type, it.network, it.security, it.port)
            },
        )
    }
}
