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
    // No default: the backend requires `inbounds` to be present, and with encodeDefaults=false
    // a default-valued field would be dropped from the JSON, causing a 400 "Validation failed".
    @SerialName("inbounds") val inbounds: List<String>
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

// ── Detail (GET by-uuid) ─────────────────────────────────────────────────────

@Serializable data class InternalSquadDetailResponse(@SerialName("response") val response: InternalSquadDetailDto)
@Serializable data class InternalSquadDetailDto(
    @SerialName("uuid")      val uuid: String,
    @SerialName("name")      val name: String,
    @SerialName("info")      val info: InternalSquadInfoDto,
    @SerialName("inbounds")  val inbounds: List<SquadInboundDto> = emptyList(),
    @SerialName("createdAt") val createdAt: String,
)
@Serializable data class SquadInboundDto(
    @SerialName("uuid")        val uuid: String,
    @SerialName("profileUuid") val profileUuid: String? = null,
    @SerialName("tag")         val tag: String,
    @SerialName("type")        val type: String,
    @SerialName("network")     val network: String? = null,
    @SerialName("security")    val security: String? = null,
    @SerialName("port")        val port: Int? = null,
)

@Serializable data class ExternalSquadDetailResponse(@SerialName("response") val response: ExternalSquadDetailDto)
@Serializable data class ExternalSquadDetailDto(
    @SerialName("uuid")                 val uuid: String,
    @SerialName("name")                 val name: String,
    @SerialName("info")                 val info: ExternalSquadInfoDto,
    @SerialName("templates")            val templates: List<SquadTemplateRefDto> = emptyList(),
    @SerialName("subscriptionSettings") val subscriptionSettings: SubscriptionSettingsOverrideDto? = null,
    @SerialName("hostOverrides")        val hostOverrides: SquadHostOverridesDto? = null,
    @SerialName("responseHeaders")      val responseHeaders: Map<String, String>? = null,
    @SerialName("hwidSettings")         val hwidSettings: HwidSettingsDto? = null,
    @SerialName("customRemarks")        val customRemarks: CustomRemarksDto? = null,
    @SerialName("subpageConfigUuid")    val subpageConfigUuid: String? = null,
    @SerialName("createdAt")            val createdAt: String,
)

@Serializable data class SquadTemplateRefDto(
    @SerialName("templateUuid") val templateUuid: String,
    @SerialName("templateType") val templateType: String,
)
@Serializable data class SubscriptionSettingsOverrideDto(
    @SerialName("profileTitle")              val profileTitle: String? = null,
    @SerialName("supportLink")               val supportLink: String? = null,
    @SerialName("profileUpdateInterval")     val profileUpdateInterval: Int? = null,
    @SerialName("isProfileWebpageUrlEnabled") val isProfileWebpageUrlEnabled: Boolean? = null,
    @SerialName("serveJsonAtBaseSubscription") val serveJsonAtBaseSubscription: Boolean? = null,
    @SerialName("isShowCustomRemarks")       val isShowCustomRemarks: Boolean? = null,
    @SerialName("happAnnounce")              val happAnnounce: String? = null,
    @SerialName("happRouting")               val happRouting: String? = null,
    @SerialName("randomizeHosts")            val randomizeHosts: Boolean? = null,
)
@Serializable data class SquadHostOverridesDto(
    @SerialName("serverDescription") val serverDescription: String? = null,
    @SerialName("vlessRouteId")      val vlessRouteId: Int? = null,
)
@Serializable data class HwidSettingsDto(
    @SerialName("enabled")              val enabled: Boolean = false,
    @SerialName("fallbackDeviceLimit")  val fallbackDeviceLimit: Int = 0,
    @SerialName("maxDevicesAnnounce")   val maxDevicesAnnounce: String? = null,
)
@Serializable data class CustomRemarksDto(
    @SerialName("expiredUsers")          val expiredUsers: List<String> = emptyList(),
    @SerialName("limitedUsers")          val limitedUsers: List<String> = emptyList(),
    @SerialName("disabledUsers")         val disabledUsers: List<String> = emptyList(),
    @SerialName("emptyHosts")            val emptyHosts: List<String> = emptyList(),
    @SerialName("HWIDMaxDevicesExceeded") val hwidMaxDevicesExceeded: List<String> = emptyList(),
    @SerialName("HWIDNotSupported")      val hwidNotSupported: List<String> = emptyList(),
)

// ── Update requests ──────────────────────────────────────────────────────────

@Serializable data class UpdateInternalSquadFullRequest(
    @SerialName("uuid")     val uuid: String,
    @SerialName("name")     val name: String,
    @SerialName("inbounds") val inbounds: List<String>,
)
@Serializable data class UpdateExternalSquadRequest(
    @SerialName("uuid")                 val uuid: String,
    @SerialName("name")                 val name: String,
    @SerialName("templates")            val templates: List<SquadTemplateRefDto>,
    @SerialName("subscriptionSettings") val subscriptionSettings: SubscriptionSettingsOverrideDto,
    @SerialName("hostOverrides")        val hostOverrides: SquadHostOverridesDto,
    @SerialName("responseHeaders")      val responseHeaders: Map<String, String>? = null,
    @SerialName("hwidSettings")         val hwidSettings: HwidSettingsDto? = null,
    @SerialName("customRemarks")        val customRemarks: CustomRemarksDto? = null,
    @SerialName("subpageConfigUuid")    val subpageConfigUuid: String? = null,
)
