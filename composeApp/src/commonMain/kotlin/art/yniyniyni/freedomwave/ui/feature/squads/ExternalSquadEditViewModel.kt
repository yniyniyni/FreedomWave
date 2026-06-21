package art.yniyniyni.freedomwave.ui.feature.squads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.repository.SquadRepository
import art.yniyniyni.freedomwave.data.repository.SubPageConfigRepository
import art.yniyniyni.freedomwave.data.repository.TemplateRepository
import art.yniyniyni.freedomwave.data.repository.UserRepository
import art.yniyniyni.freedomwave.domain.model.SquadMember
import art.yniyniyni.freedomwave.domain.model.SubPageConfig
import art.yniyniyni.freedomwave.domain.model.SubscriptionTemplate
import art.yniyniyni.freedomwave.ui.l10n.UiText
import art.yniyniyni.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RemarkCategory {
    EXPIRED, LIMITED, DISABLED, EMPTY_HOSTS, HWID_MAX, HWID_UNSUPPORTED
}

data class ExternalSquadEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val actionError: UiText? = null,

    // Loaded reference data
    val templatesByType: Map<String, List<SubscriptionTemplate>> = emptyMap(),
    val subPageConfigs: List<SubPageConfig> = emptyList(),

    // Name
    val name: String = "",

    // Templates: templateType -> templateUuid (only those chosen)
    val selectedTemplates: Map<String, String> = emptyMap(),

    // Subscription settings overrides (null = override OFF)
    val profileTitle: String? = null,
    val supportLink: String? = null,
    val profileUpdateInterval: String? = null, // null=off; non-null keeps text-field string
    val isProfileWebpageUrlEnabled: Boolean? = null,
    val serveJsonAtBaseSubscription: Boolean? = null,
    val isShowCustomRemarks: Boolean? = null,
    val happAnnounce: String? = null,
    val happRouting: String? = null,
    val randomizeHosts: Boolean? = null,

    // Host overrides (null = override OFF)
    val serverDescription: String? = null,
    val vlessRouteId: String? = null, // null=off; non-null keeps text-field string

    // Response headers
    val headers: List<Pair<String, String>> = emptyList(),

    // HWID settings
    val hwidOverride: Boolean = false,
    val hwidEnabled: Boolean = false,
    val hwidFallbackDeviceLimit: String = "0",
    val hwidMaxDevicesAnnounce: String = "",

    // Custom remarks
    val remarksOverride: Boolean = false,
    val expiredUsers: List<String> = emptyList(),
    val limitedUsers: List<String> = emptyList(),
    val disabledUsers: List<String> = emptyList(),
    val emptyHosts: List<String> = emptyList(),
    val hwidMaxDevicesExceeded: List<String> = emptyList(),
    val hwidNotSupported: List<String> = emptyList(),

    // Subpage config
    val subpageConfigUuid: String? = null,

    // Members section (lazy)
    val memberCount: Int = 0,
    val membersExpanded: Boolean = false,
    val membersLoading: Boolean = false,
    val membersLoaded: Boolean = false,
    val members: List<SquadMember> = emptyList(),
    val membersError: UiText? = null,
) {
    val canSave: Boolean
        get() = nameValid(name) && remarksValid(toInput()) && !isSaving

    val remarksError: Boolean
        get() = remarksOverride && !remarksValid(toInput())

    fun toInput(): ExternalSquadFormInput = ExternalSquadFormInput(
        name = name,
        templates = selectedTemplates,
        profileTitle = profileTitle,
        supportLink = supportLink,
        profileUpdateInterval = profileUpdateInterval?.let { updateIntervalOrNull(it) },
        isProfileWebpageUrlEnabled = isProfileWebpageUrlEnabled,
        serveJsonAtBaseSubscription = serveJsonAtBaseSubscription,
        isShowCustomRemarks = isShowCustomRemarks,
        happAnnounce = happAnnounce,
        happRouting = happRouting,
        randomizeHosts = randomizeHosts,
        serverDescription = serverDescription,
        vlessRouteId = vlessRouteId?.let { squadVlessRouteIdOrNull(it) },
        headers = headers,
        hwidOverride = hwidOverride,
        hwidEnabled = hwidEnabled,
        hwidFallbackDeviceLimit = hwidFallbackDeviceLimit.toIntOrNull() ?: 0,
        hwidMaxDevicesAnnounce = hwidMaxDevicesAnnounce.ifBlank { null },
        remarksOverride = remarksOverride,
        expiredUsers = expiredUsers,
        limitedUsers = limitedUsers,
        disabledUsers = disabledUsers,
        emptyHosts = emptyHosts,
        hwidMaxDevicesExceeded = hwidMaxDevicesExceeded,
        hwidNotSupported = hwidNotSupported,
        subpageConfigUuid = subpageConfigUuid,
    )
}

class ExternalSquadEditViewModel(
    private val squadUuid: String,
    private val squadRepo: SquadRepository,
    private val templateRepo: TemplateRepository,
    private val subPageRepo: SubPageConfigRepository,
    private val userRepo: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExternalSquadEditUiState())
    val state: StateFlow<ExternalSquadEditUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val templatesByType = templateRepo.getTemplatesByType().getOrDefault(emptyMap())
            val subPageConfigs = subPageRepo.getConfigs().getOrDefault(emptyList())

            squadRepo.getExternalSquadDetail(squadUuid)
                .onSuccess { d ->
                    _state.update { s ->
                        val ss = d.subscriptionSettings
                        val ho = d.hostOverrides
                        val hw = d.hwidSettings
                        val cr = d.customRemarks
                        s.copy(
                            isLoading = false,
                            templatesByType = templatesByType,
                            subPageConfigs = subPageConfigs,
                            name = d.name,
                            selectedTemplates = d.templates.associate { it.templateType to it.templateUuid },
                            // Subscription settings: non-null DTO field → enable override
                            profileTitle = ss?.profileTitle,
                            supportLink = ss?.supportLink,
                            profileUpdateInterval = ss?.profileUpdateInterval?.toString(),
                            isProfileWebpageUrlEnabled = ss?.isProfileWebpageUrlEnabled,
                            serveJsonAtBaseSubscription = ss?.serveJsonAtBaseSubscription,
                            isShowCustomRemarks = ss?.isShowCustomRemarks,
                            happAnnounce = ss?.happAnnounce,
                            happRouting = ss?.happRouting,
                            randomizeHosts = ss?.randomizeHosts,
                            // Host overrides
                            serverDescription = ho?.serverDescription,
                            vlessRouteId = ho?.vlessRouteId?.toString(),
                            // Response headers
                            headers = d.responseHeaders?.entries?.map { it.key to it.value } ?: emptyList(),
                            // HWID
                            hwidOverride = hw != null,
                            hwidEnabled = hw?.enabled ?: false,
                            hwidFallbackDeviceLimit = hw?.fallbackDeviceLimit?.toString() ?: "0",
                            hwidMaxDevicesAnnounce = hw?.maxDevicesAnnounce ?: "",
                            // Remarks
                            remarksOverride = cr != null,
                            expiredUsers = cr?.expiredUsers ?: emptyList(),
                            limitedUsers = cr?.limitedUsers ?: emptyList(),
                            disabledUsers = cr?.disabledUsers ?: emptyList(),
                            emptyHosts = cr?.emptyHosts ?: emptyList(),
                            hwidMaxDevicesExceeded = cr?.hwidMaxDevicesExceeded ?: emptyList(),
                            hwidNotSupported = cr?.hwidNotSupported ?: emptyList(),
                            subpageConfigUuid = d.subpageConfigUuid,
                            memberCount = d.membersCount,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            actionError = e.toUiText(),
                            templatesByType = templatesByType,
                            subPageConfigs = subPageConfigs,
                        )
                    }
                }
        }
    }

    // Name

    fun onName(v: String) = _state.update { it.copy(name = v, actionError = null) }

    // Templates

    fun selectTemplate(type: String, uuid: String?) = _state.update { s ->
        s.copy(
            selectedTemplates = if (uuid == null)
                s.selectedTemplates - type
            else
                s.selectedTemplates + (type to uuid)
        )
    }

    // Subscription settings overrides
    // Pattern: toggle(on) sets a default when enabling, null when disabling.
    // onX(v) updates the text-field value only (no-op if override is off).

    fun toggleProfileTitle(on: Boolean) = _state.update { it.copy(profileTitle = if (on) "" else null) }
    fun onProfileTitle(v: String) = _state.update { it.copy(profileTitle = v) }

    fun toggleSupportLink(on: Boolean) = _state.update { it.copy(supportLink = if (on) "" else null) }
    fun onSupportLink(v: String) = _state.update { it.copy(supportLink = v) }

    fun toggleProfileUpdateInterval(on: Boolean) =
        _state.update { it.copy(profileUpdateInterval = if (on) "" else null) }
    fun onProfileUpdateInterval(v: String) =
        _state.update { it.copy(profileUpdateInterval = v.filter { c -> c.isDigit() }) }

    fun toggleIsProfileWebpageUrlEnabled(on: Boolean) =
        _state.update { it.copy(isProfileWebpageUrlEnabled = if (on) false else null) }
    fun setIsProfileWebpageUrlEnabled(v: Boolean) =
        _state.update { it.copy(isProfileWebpageUrlEnabled = v) }

    fun toggleServeJsonAtBaseSubscription(on: Boolean) =
        _state.update { it.copy(serveJsonAtBaseSubscription = if (on) false else null) }
    fun setServeJsonAtBaseSubscription(v: Boolean) =
        _state.update { it.copy(serveJsonAtBaseSubscription = v) }

    fun toggleIsShowCustomRemarks(on: Boolean) =
        _state.update { it.copy(isShowCustomRemarks = if (on) false else null) }
    fun setIsShowCustomRemarks(v: Boolean) =
        _state.update { it.copy(isShowCustomRemarks = v) }

    fun toggleHappAnnounce(on: Boolean) = _state.update { it.copy(happAnnounce = if (on) "" else null) }
    fun onHappAnnounce(v: String) = _state.update { it.copy(happAnnounce = v) }

    fun toggleHappRouting(on: Boolean) = _state.update { it.copy(happRouting = if (on) "" else null) }
    fun onHappRouting(v: String) = _state.update { it.copy(happRouting = v) }

    fun toggleRandomizeHosts(on: Boolean) =
        _state.update { it.copy(randomizeHosts = if (on) false else null) }
    fun setRandomizeHosts(v: Boolean) =
        _state.update { it.copy(randomizeHosts = v) }

    // Host overrides

    fun toggleServerDescription(on: Boolean) =
        _state.update { it.copy(serverDescription = if (on) "" else null) }
    fun onServerDescription(v: String) = _state.update { it.copy(serverDescription = v) }

    fun toggleVlessRouteId(on: Boolean) =
        _state.update { it.copy(vlessRouteId = if (on) "" else null) }
    fun onVlessRouteId(v: String) =
        _state.update { it.copy(vlessRouteId = v.filter { c -> c.isDigit() }) }

    // Response headers

    fun addHeader() = _state.update { it.copy(headers = it.headers + ("" to "")) }

    fun updateHeaderName(i: Int, v: String) = _state.update { s ->
        s.copy(headers = s.headers.toMutableList().also { list ->
            if (i in list.indices) list[i] = v to list[i].second
        })
    }

    fun updateHeaderValue(i: Int, v: String) = _state.update { s ->
        s.copy(headers = s.headers.toMutableList().also { list ->
            if (i in list.indices) list[i] = list[i].first to v
        })
    }

    fun removeHeader(i: Int) = _state.update { s ->
        s.copy(headers = s.headers.toMutableList().also { list ->
            if (i in list.indices) list.removeAt(i)
        })
    }

    // HWID

    fun setHwidOverride(v: Boolean) = _state.update { it.copy(hwidOverride = v) }
    fun setHwidEnabled(v: Boolean) = _state.update { it.copy(hwidEnabled = v) }
    fun onHwidFallback(v: String) =
        _state.update { it.copy(hwidFallbackDeviceLimit = v.filter { c -> c.isDigit() }) }
    fun onHwidAnnounce(v: String) = _state.update { it.copy(hwidMaxDevicesAnnounce = v) }

    // Remarks

    fun setRemarksOverride(v: Boolean) = _state.update { it.copy(remarksOverride = v) }

    fun addRemark(cat: RemarkCategory) = _state.update { s ->
        s.copyRemarks(cat) { it + "" }
    }

    fun updateRemark(cat: RemarkCategory, i: Int, v: String) = _state.update { s ->
        s.copyRemarks(cat) { list ->
            list.toMutableList().also { ml -> if (i in ml.indices) ml[i] = v }
        }
    }

    fun removeRemark(cat: RemarkCategory, i: Int) = _state.update { s ->
        s.copyRemarks(cat) { list ->
            list.toMutableList().also { ml -> if (i in ml.indices) ml.removeAt(i) }
        }
    }

    private fun ExternalSquadEditUiState.copyRemarks(
        cat: RemarkCategory,
        transform: (List<String>) -> List<String>,
    ): ExternalSquadEditUiState = when (cat) {
        RemarkCategory.EXPIRED        -> copy(expiredUsers = transform(expiredUsers))
        RemarkCategory.LIMITED        -> copy(limitedUsers = transform(limitedUsers))
        RemarkCategory.DISABLED       -> copy(disabledUsers = transform(disabledUsers))
        RemarkCategory.EMPTY_HOSTS    -> copy(emptyHosts = transform(emptyHosts))
        RemarkCategory.HWID_MAX       -> copy(hwidMaxDevicesExceeded = transform(hwidMaxDevicesExceeded))
        RemarkCategory.HWID_UNSUPPORTED -> copy(hwidNotSupported = transform(hwidNotSupported))
    }

    // Subpage config

    fun selectSubpage(uuid: String?) = _state.update { it.copy(subpageConfigUuid = uuid) }

    // Error

    fun clearError() = _state.update { it.copy(actionError = null) }

    /** Expand/collapse the members section; fetch on first expand. */
    fun toggleMembers() {
        val s = _state.value
        val nowExpanded = !s.membersExpanded
        _state.update { it.copy(membersExpanded = nowExpanded) }
        if (nowExpanded && !s.membersLoaded && !s.membersLoading) loadMembers()
    }

    fun loadMembers() {
        _state.update { it.copy(membersLoading = true, membersError = null) }
        viewModelScope.launch {
            userRepo.getUsers()
                .onSuccess { users ->
                    _state.update {
                        it.copy(
                            membersLoading = false,
                            membersLoaded = true,
                            members = SquadMember.externalMembers(users, squadUuid),
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(membersLoading = false, membersError = e.toUiText()) } }
        }
    }

    // Submit

    fun submit(onSaved: () -> Unit) {
        if (!_state.value.canSave) return
        _state.update { it.copy(isSaving = true, actionError = null) }
        viewModelScope.launch {
            squadRepo.updateExternalSquadFull(
                buildExternalSquadRequest(squadUuid, _state.value.toInput())
            ).onSuccess {
                _state.update { it.copy(isSaving = false) }
                onSaved()
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, actionError = e.toUiText()) }
            }
        }
    }
}
