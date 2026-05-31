package art.yniyniyni.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Internal squads ──────────────────────────────────────────────────────────

@Serializable
data class InternalSquadListResponse(
    @SerialName("response") val response: InternalSquadListData
)

@Serializable
data class InternalSquadListData(
    @SerialName("total")          val total: Int,
    @SerialName("internalSquads") val internalSquads: List<InternalSquadDto>
)

@Serializable
data class InternalSquadResponse(
    @SerialName("response") val response: InternalSquadDto
)

@Serializable
data class InternalSquadDto(
    @SerialName("uuid")         val uuid: String,
    @SerialName("name")         val name: String,
    @SerialName("viewPosition") val viewPosition: Int = 0,
    @SerialName("info")         val info: InternalSquadInfoDto,
    @SerialName("createdAt")    val createdAt: String
)

@Serializable
data class InternalSquadInfoDto(
    @SerialName("membersCount")  val membersCount: Int,
    @SerialName("inboundsCount") val inboundsCount: Int
)

// ── External squads ──────────────────────────────────────────────────────────

@Serializable
data class ExternalSquadListResponse(
    @SerialName("response") val response: ExternalSquadListData
)

@Serializable
data class ExternalSquadListData(
    @SerialName("total")          val total: Int,
    @SerialName("externalSquads") val externalSquads: List<ExternalSquadDto>
)

@Serializable
data class ExternalSquadResponse(
    @SerialName("response") val response: ExternalSquadDto
)

@Serializable
data class ExternalSquadDto(
    @SerialName("uuid")         val uuid: String,
    @SerialName("name")         val name: String,
    @SerialName("viewPosition") val viewPosition: Int = 0,
    @SerialName("info")         val info: ExternalSquadInfoDto,
    @SerialName("createdAt")    val createdAt: String
)

@Serializable
data class ExternalSquadInfoDto(
    @SerialName("membersCount") val membersCount: Int
)

// ── Requests ─────────────────────────────────────────────────────────────────

@Serializable
data class CreateInternalSquadRequest(
    @SerialName("name")     val name: String,
    @SerialName("inbounds") val inbounds: List<String> = emptyList()
)

@Serializable
data class CreateExternalSquadRequest(
    @SerialName("name") val name: String
)

@Serializable
data class UpdateSquadRequest(
    @SerialName("uuid") val uuid: String,
    @SerialName("name") val name: String
)
