package art.yniyniyni.freedomwave.ui.feature.nodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.repository.ConfigProfileRepository
import art.yniyniyni.freedomwave.data.repository.NodeRepository
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.ConfigProfile
import art.yniyniyni.freedomwave.ui.l10n.UiText
import art.yniyniyni.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NodeFormUiState(
    val isEdit: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val actionError: UiText? = null,
    val profiles: List<ConfigProfile> = emptyList(),
    val secretKey: String? = null,

    val name: String = "",
    val address: String = "",
    val port: String = "",
    val countryCode: String = "XX",
    val selectedProfileUuid: String? = null,
    val selectedInbounds: Set<String> = emptySet(),
    val trackingActive: Boolean = false,
    val multiplier: String = "",
    val trafficLimitGb: String = "",
    val resetDay: String = "",
    val notifyPercent: String = "",
    val tags: String = "",
) {
    val nameError: UiText?
        get() = if (name.isNotBlank() && !nameValid(name)) UiText.Raw("3–30 characters") else null
    val addressError: UiText?
        get() = if (address.isNotBlank() && !addressValid(address)) UiText.Raw("Min. 2 characters") else null
    val portError: UiText?
        get() = if (port.isNotBlank() && portOrNull(port) == null) UiText.Raw("Port 1–65535") else null

    val selectedProfile: ConfigProfile?
        get() = profiles.find { it.uuid == selectedProfileUuid }

    val canSave: Boolean
        get() = nameValid(name) && addressValid(address) && portError == null &&
            selectedProfileUuid != null && !isSaving
}

class NodeFormViewModel(
    private val nodeUuid: String?,
    private val nodeRepo: NodeRepository,
    private val configRepo: ConfigProfileRepository,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(NodeFormUiState(isEdit = nodeUuid != null, isLoading = true))
    val state: StateFlow<NodeFormUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val profiles = configRepo.getProfiles().getOrDefault(emptyList())
            val secret = configRepo.getSecretKey().getOrNull()
            _state.update { it.copy(profiles = profiles, secretKey = secret) }

            if (nodeUuid != null) {
                nodeRepo.getNode(nodeUuid).onSuccess { node ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            name = node.name,
                            address = node.address,
                            port = node.port?.toString() ?: "",
                            countryCode = node.countryCode.ifBlank { "XX" },
                            selectedProfileUuid = node.activeConfigProfileUuid ?: profiles.firstOrNull()?.uuid,
                            selectedInbounds = node.activeInbounds.toSet(),
                            trackingActive = node.isTrafficTrackingActive,
                            multiplier = node.consumptionMultiplier.toString(),
                            trafficLimitGb = node.trafficLimitBytes?.let { b -> (b.toDouble() / 1_073_741_824L).toString() } ?: "",
                            resetDay = node.trafficResetDay?.toString() ?: "",
                            notifyPercent = node.notifyPercent?.toString() ?: "",
                            tags = node.tags.joinToString(", "),
                        )
                    }
                }.onFailure { e ->
                    _state.update { it.copy(isLoading = false, actionError = e.toUiText()) }
                }
            } else {
                val first = profiles.firstOrNull()
                _state.update {
                    it.copy(
                        isLoading = false,
                        selectedProfileUuid = first?.uuid,
                        selectedInbounds = first?.inbounds?.map { ib -> ib.uuid }?.toSet() ?: emptySet(),
                    )
                }
            }
        }
    }

    fun onName(v: String)           = _state.update { it.copy(name = v, actionError = null) }
    fun onAddress(v: String)        = _state.update { it.copy(address = v, actionError = null) }
    fun onPort(v: String)           = _state.update { it.copy(port = v.filter { c -> c.isDigit() }) }
    fun onCountry(code: String)     = _state.update { it.copy(countryCode = code) }
    fun onMultiplier(v: String)     = _state.update { it.copy(multiplier = v) }
    fun onTrafficLimitGb(v: String) = _state.update { it.copy(trafficLimitGb = v) }
    fun onResetDay(v: String)       = _state.update { it.copy(resetDay = v.filter { c -> c.isDigit() }) }
    fun onNotifyPercent(v: String)  = _state.update { it.copy(notifyPercent = v.filter { c -> c.isDigit() }) }
    fun onTags(v: String)           = _state.update { it.copy(tags = v) }
    fun setTracking(v: Boolean)     = _state.update { it.copy(trackingActive = v) }

    fun selectProfile(uuid: String) = _state.update { s ->
        val inbounds = s.profiles.find { it.uuid == uuid }?.inbounds?.map { it.uuid }?.toSet() ?: emptySet()
        s.copy(selectedProfileUuid = uuid, selectedInbounds = inbounds)
    }

    fun toggleInbound(uuid: String) = _state.update { s ->
        s.copy(selectedInbounds = if (uuid in s.selectedInbounds) s.selectedInbounds - uuid else s.selectedInbounds + uuid)
    }

    fun selectAllInbounds() = _state.update { s ->
        s.copy(selectedInbounds = s.selectedProfile?.inbounds?.map { it.uuid }?.toSet() ?: emptySet())
    }

    fun deselectAllInbounds() = _state.update { it.copy(selectedInbounds = emptySet()) }

    fun clearError() = _state.update { it.copy(actionError = null) }

    private fun currentInput(s: NodeFormUiState): NodeFormInput = NodeFormInput(
        name = s.name,
        address = s.address,
        port = portOrNull(s.port),
        countryCode = s.countryCode,
        trackingActive = s.trackingActive,
        multiplier = multiplierOrNull(s.multiplier),
        trafficLimitBytes = gbToBytes(s.trafficLimitGb),
        resetDay = resetDayOrNull(s.resetDay),
        notifyPercent = notifyPercentOrNull(s.notifyPercent),
        tags = parseTags(s.tags),
        profileUuid = s.selectedProfileUuid.orEmpty(),
        inbounds = s.selectedInbounds.toList(),
    )

    fun submit(onSaved: () -> Unit) {
        val s = _state.value
        if (!s.canSave) return
        val input = currentInput(s)
        _state.update { it.copy(isSaving = true, actionError = null) }
        viewModelScope.launch {
            val result = if (nodeUuid == null) nodeRepo.createNode(buildCreateRequest(input))
            else nodeRepo.updateNode(buildUpdateRequest(nodeUuid, input))
            result.onSuccess {
                _state.update { it.copy(isSaving = false) }
                onSaved()
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, actionError = e.toUiText()) }
            }
        }
    }
}
