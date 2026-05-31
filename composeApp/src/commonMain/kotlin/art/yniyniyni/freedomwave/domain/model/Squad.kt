package art.yniyniyni.freedomwave.domain.model

import art.yniyniyni.freedomwave.data.api.dto.ExternalSquadDto
import art.yniyniyni.freedomwave.data.api.dto.InternalSquadDto

data class Squad(
    val uuid: String,
    val name: String,
    val type: Type,
    val membersCount: Int,
    val inboundsCount: Int?,
    val createdAt: String
) {
    enum class Type { INTERNAL, EXTERNAL }

    companion object {
        fun from(dto: InternalSquadDto) = Squad(
            uuid          = dto.uuid,
            name          = dto.name,
            type          = Type.INTERNAL,
            membersCount  = dto.info.membersCount,
            inboundsCount = dto.info.inboundsCount,
            createdAt     = dto.createdAt
        )

        fun from(dto: ExternalSquadDto) = Squad(
            uuid          = dto.uuid,
            name          = dto.name,
            type          = Type.EXTERNAL,
            membersCount  = dto.info.membersCount,
            inboundsCount = null,
            createdAt     = dto.createdAt
        )
    }
}
