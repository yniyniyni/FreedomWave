package org.freedomwave.domain.model

import org.freedomwave.data.api.dto.UserDto

enum class UserStatus(val apiValue: String) {
    ACTIVE("ACTIVE"),
    DISABLED("DISABLED"),
    LIMITED("LIMITED"),
    EXPIRED("EXPIRED");

    companion object {
        fun from(value: String) = entries.find { it.apiValue == value } ?: DISABLED
    }
}

data class User(
    /** Null on panel 3.x, which removed user uuids. Prefer [userRef] for identity. */
    val uuid: String?,
    val id: Int,
    val shortUuid: String,
    val username: String,
    val status: UserStatus,
    val trafficLimitBytes: Long,
    val trafficLimitStrategy: String,
    val expireAt: String,
    val usedTrafficBytes: Long,
    val lifetimeUsedTrafficBytes: Long,
    val subscriptionUrl: String,
    val onlineAt: String?,
    val email: String?,
    val tag: String?,
    val description: String?,
    val telegramId: Long?,
    val hwidDeviceLimit: Int?,
    val activeSquads: List<String>,
    val activeSquadUuids: List<String>,
    val externalSquadUuid: String?,
    val lastConnectedNodeUuid: String?,
    val createdAt: String
) {
    /**
     * The identifier this user's panel expects back — the path segment for every user route
     * and the target of an update body.
     *
     * Derived from the payload rather than from a stored panel version, so it is always
     * consistent with the server that actually answered: 2.8.x sends a uuid and keys routes by
     * it, 3.x sends no uuid and keys routes by `id`. A consequence worth relying on: a 3.x ref
     * always parses as an Int and a uuid never does, which is how the repository decides
     * whether an update body carries `id` or `uuid`.
     */
    val userRef: String get() = uuid ?: id.toString()

    val isActive: Boolean   get() = status == UserStatus.ACTIVE
    val isDisabled: Boolean get() = status == UserStatus.DISABLED

    companion object {
        fun from(dto: UserDto) = User(
            uuid                     = dto.uuid,
            id                       = dto.id,
            shortUuid                = dto.shortUuid,
            username                 = dto.username,
            status                   = UserStatus.from(dto.status),
            trafficLimitBytes        = dto.trafficLimitBytes,
            trafficLimitStrategy     = dto.trafficLimitStrategy,
            expireAt                 = dto.expireAt,
            usedTrafficBytes         = dto.userTraffic.usedTrafficBytes,
            lifetimeUsedTrafficBytes = dto.userTraffic.lifetimeUsedTrafficBytes,
            subscriptionUrl          = dto.subscriptionUrl,
            onlineAt                 = dto.userTraffic.onlineAt,
            email                    = dto.email,
            tag                      = dto.tag,
            description              = dto.description,
            telegramId               = dto.telegramId,
            hwidDeviceLimit          = dto.hwidDeviceLimit,
            activeSquads             = dto.activeInternalSquads.map { it.name },
            activeSquadUuids         = dto.activeInternalSquads.map { it.uuid },
            externalSquadUuid        = dto.externalSquadUuid,
            lastConnectedNodeUuid    = dto.userTraffic.lastConnectedNodeUuid,
            createdAt                = dto.createdAt
        )
    }
}
