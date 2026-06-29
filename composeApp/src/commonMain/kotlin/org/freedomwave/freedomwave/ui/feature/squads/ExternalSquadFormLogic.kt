package org.freedomwave.ui.feature.squads

import org.freedomwave.data.api.dto.CustomRemarksDto
import org.freedomwave.data.api.dto.HwidSettingsDto
import org.freedomwave.data.api.dto.SquadHostOverridesDto
import org.freedomwave.data.api.dto.SquadTemplateRefDto
import org.freedomwave.data.api.dto.SubscriptionSettingsOverrideDto
import org.freedomwave.data.api.dto.UpdateExternalSquadRequest

/** A null field = override disabled (not sent). Non-null = override enabled with that value. */
data class ExternalSquadFormInput(
    val name: String,
    val templates: Map<String, String>,          // templateType -> templateUuid (only chosen)
    val profileTitle: String?,
    val supportLink: String?,
    val profileUpdateInterval: Int?,
    val isProfileWebpageUrlEnabled: Boolean?,
    val serveJsonAtBaseSubscription: Boolean?,
    val isShowCustomRemarks: Boolean?,
    val happAnnounce: String?,
    val happRouting: String?,
    val randomizeHosts: Boolean?,
    val serverDescription: String?,              // host override
    val vlessRouteId: Int?,                      // host override
    val headers: List<Pair<String, String>>,
    val hwidOverride: Boolean,                   // master toggle
    val hwidEnabled: Boolean,
    val hwidFallbackDeviceLimit: Int,
    val hwidMaxDevicesAnnounce: String?,
    val remarksOverride: Boolean,                // master toggle
    val expiredUsers: List<String>,
    val limitedUsers: List<String>,
    val disabledUsers: List<String>,
    val emptyHosts: List<String>,
    val hwidMaxDevicesExceeded: List<String>,
    val hwidNotSupported: List<String>,
    val subpageConfigUuid: String?,
)

fun nameValid(s: String): Boolean = s.trim().length in 2..30
fun updateIntervalOrNull(s: String): Int? { val n = s.trim().toIntOrNull() ?: return null; return if (n >= 1) n else null }
fun announceValid(s: String): Boolean = s.length <= 200
fun squadVlessRouteIdOrNull(s: String): Int? { if (s.isBlank()) return null; val n = s.trim().toIntOrNull() ?: return null; return if (n in 0..65535) n else null }

private fun List<String>.cleaned(): List<String> = map { it.trim() }.filter { it.isNotEmpty() }

/** When remarks override is on, every one of the 6 lists must have >= 1 non-blank entry. */
fun remarksValid(input: ExternalSquadFormInput): Boolean {
    if (!input.remarksOverride) return true
    return listOf(
        input.expiredUsers, input.limitedUsers, input.disabledUsers,
        input.emptyHosts, input.hwidMaxDevicesExceeded, input.hwidNotSupported,
    ).all { it.cleaned().isNotEmpty() }
}

fun buildExternalSquadRequest(uuid: String, input: ExternalSquadFormInput): UpdateExternalSquadRequest {
    val headersMap = input.headers
        .map { it.first.trim() to it.second }
        .filter { it.first.isNotEmpty() }
        .toMap()
    return UpdateExternalSquadRequest(
        uuid = uuid,
        name = input.name.trim(),
        templates = input.templates.filterValues { it.isNotBlank() }
            .map { (type, tplUuid) -> SquadTemplateRefDto(templateUuid = tplUuid, templateType = type) },
        subscriptionSettings = SubscriptionSettingsOverrideDto(
            profileTitle = input.profileTitle,
            supportLink = input.supportLink,
            profileUpdateInterval = input.profileUpdateInterval,
            isProfileWebpageUrlEnabled = input.isProfileWebpageUrlEnabled,
            serveJsonAtBaseSubscription = input.serveJsonAtBaseSubscription,
            isShowCustomRemarks = input.isShowCustomRemarks,
            happAnnounce = input.happAnnounce,
            happRouting = input.happRouting,
            randomizeHosts = input.randomizeHosts,
        ),
        hostOverrides = SquadHostOverridesDto(
            serverDescription = input.serverDescription,
            vlessRouteId = input.vlessRouteId,
        ),
        responseHeaders = headersMap.ifEmpty { null },
        hwidSettings = if (input.hwidOverride) HwidSettingsDto(
            enabled = input.hwidEnabled,
            fallbackDeviceLimit = input.hwidFallbackDeviceLimit,
            maxDevicesAnnounce = input.hwidMaxDevicesAnnounce?.trim()?.ifBlank { null },
        ) else null,
        customRemarks = if (input.remarksOverride) CustomRemarksDto(
            expiredUsers = input.expiredUsers.cleaned(),
            limitedUsers = input.limitedUsers.cleaned(),
            disabledUsers = input.disabledUsers.cleaned(),
            emptyHosts = input.emptyHosts.cleaned(),
            hwidMaxDevicesExceeded = input.hwidMaxDevicesExceeded.cleaned(),
            hwidNotSupported = input.hwidNotSupported.cleaned(),
        ) else null,
        subpageConfigUuid = input.subpageConfigUuid,
    )
}
