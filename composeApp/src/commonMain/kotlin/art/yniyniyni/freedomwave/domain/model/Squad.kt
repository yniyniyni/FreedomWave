package art.yniyniyni.freedomwave.domain.model

import art.yniyniyni.freedomwave.data.api.dto.ExternalSquadDetailDto
import art.yniyniyni.freedomwave.data.api.dto.ExternalSquadDto
import art.yniyniyni.freedomwave.data.api.dto.InternalSquadDetailDto
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

data class SquadInbound(
    val uuid: String,
    val tag: String,
    val type: String,
    val network: String?,
    val security: String?,
    val port: Int?,
)

/** A user belonging to a squad — the minimal projection needed for the members list. */
data class SquadMember(
    val uuid: String,
    val username: String,
    val status: UserStatus,
) {
    companion object {
        private fun List<User>.toMembers(): List<SquadMember> =
            map { SquadMember(it.uuid, it.username, it.status) }.sortedBy { it.username.lowercase() }

        /** Users whose internal-squad membership includes [squadUuid]. */
        fun internalMembers(users: List<User>, squadUuid: String): List<SquadMember> =
            users.filter { squadUuid in it.activeSquadUuids }.toMembers()

        /** Users whose single external squad is [squadUuid]. */
        fun externalMembers(users: List<User>, squadUuid: String): List<SquadMember> =
            users.filter { it.externalSquadUuid == squadUuid }.toMembers()
    }
}

data class InternalSquadDetail(
    val uuid: String,
    val name: String,
    val membersCount: Int,
    val inboundUuids: List<String>,
) {
    companion object {
        fun from(dto: InternalSquadDetailDto) = InternalSquadDetail(
            uuid         = dto.uuid,
            name         = dto.name,
            membersCount = dto.info.membersCount,
            inboundUuids = dto.inbounds.map { it.uuid },
        )
    }
}

data class SquadTemplateRef(val templateUuid: String, val templateType: String)

data class SubscriptionSettingsOverride(
    val profileTitle: String?,
    val supportLink: String?,
    val profileUpdateInterval: Int?,
    val isProfileWebpageUrlEnabled: Boolean?,
    val serveJsonAtBaseSubscription: Boolean?,
    val isShowCustomRemarks: Boolean?,
    val happAnnounce: String?,
    val happRouting: String?,
    val randomizeHosts: Boolean?,
)

data class SquadHostOverrides(val serverDescription: String?, val vlessRouteId: Int?)

data class HwidSettings(val enabled: Boolean, val fallbackDeviceLimit: Int, val maxDevicesAnnounce: String?)

data class CustomRemarks(
    val expiredUsers: List<String>,
    val limitedUsers: List<String>,
    val disabledUsers: List<String>,
    val emptyHosts: List<String>,
    val hwidMaxDevicesExceeded: List<String>,
    val hwidNotSupported: List<String>,
)

data class ExternalSquadDetail(
    val uuid: String,
    val name: String,
    val membersCount: Int,
    val templates: List<SquadTemplateRef>,
    val subscriptionSettings: SubscriptionSettingsOverride?,
    val hostOverrides: SquadHostOverrides?,
    val responseHeaders: Map<String, String>?,
    val hwidSettings: HwidSettings?,
    val customRemarks: CustomRemarks?,
    val subpageConfigUuid: String?,
) {
    companion object {
        fun from(dto: ExternalSquadDetailDto) = ExternalSquadDetail(
            uuid         = dto.uuid,
            name         = dto.name,
            membersCount = dto.info.membersCount,
            templates    = dto.templates.map { SquadTemplateRef(it.templateUuid, it.templateType) },
            subscriptionSettings = dto.subscriptionSettings?.let {
                SubscriptionSettingsOverride(
                    it.profileTitle, it.supportLink, it.profileUpdateInterval,
                    it.isProfileWebpageUrlEnabled, it.serveJsonAtBaseSubscription, it.isShowCustomRemarks,
                    it.happAnnounce, it.happRouting, it.randomizeHosts,
                )
            },
            hostOverrides  = dto.hostOverrides?.let { SquadHostOverrides(it.serverDescription, it.vlessRouteId) },
            responseHeaders = dto.responseHeaders,
            hwidSettings   = dto.hwidSettings?.let { HwidSettings(it.enabled, it.fallbackDeviceLimit, it.maxDevicesAnnounce) },
            customRemarks  = dto.customRemarks?.let {
                CustomRemarks(it.expiredUsers, it.limitedUsers, it.disabledUsers, it.emptyHosts, it.hwidMaxDevicesExceeded, it.hwidNotSupported)
            },
            subpageConfigUuid = dto.subpageConfigUuid,
        )
    }
}
