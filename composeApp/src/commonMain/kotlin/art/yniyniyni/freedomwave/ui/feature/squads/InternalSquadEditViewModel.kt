package art.yniyniyni.freedomwave.ui.feature.squads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.api.dto.UpdateInternalSquadFullRequest
import art.yniyniyni.freedomwave.data.repository.ConfigProfileRepository
import art.yniyniyni.freedomwave.data.repository.SquadRepository
import art.yniyniyni.freedomwave.data.repository.UserRepository
import art.yniyniyni.freedomwave.domain.model.ConfigProfile
import art.yniyniyni.freedomwave.domain.model.SquadMember
import art.yniyniyni.freedomwave.ui.l10n.UiText
import art.yniyniyni.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InternalSquadEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val actionError: UiText? = null,
    val name: String = "",
    val profiles: List<ConfigProfile> = emptyList(),
    val selectedInbounds: Set<String> = emptySet(),

    // Members section (lazy)
    val memberCount: Int = 0,
    val membersExpanded: Boolean = false,
    val membersLoading: Boolean = false,
    val membersLoaded: Boolean = false,
    val members: List<SquadMember> = emptyList(),
    val membersError: UiText? = null,
) {
    val canSave: Boolean
        get() = nameValid(name) && !isSaving
}

class InternalSquadEditViewModel(
    private val squadUuid: String,
    private val squadRepo: SquadRepository,
    private val configRepo: ConfigProfileRepository,
    private val userRepo: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(InternalSquadEditUiState())
    val state: StateFlow<InternalSquadEditUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val profiles = configRepo.getProfiles().getOrDefault(emptyList())
            squadRepo.getInternalSquadDetail(squadUuid)
                .onSuccess { d ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            name = d.name,
                            selectedInbounds = d.inboundUuids.toSet(),
                            profiles = profiles,
                            memberCount = d.membersCount,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, actionError = e.toUiText(), profiles = profiles) }
                }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v, actionError = null) }

    fun toggleInbound(uuid: String) = _state.update { s ->
        s.copy(
            selectedInbounds = if (uuid in s.selectedInbounds)
                s.selectedInbounds - uuid
            else
                s.selectedInbounds + uuid
        )
    }

    fun selectAllInbounds(profileUuid: String) = _state.update { s ->
        val toAdd = s.profiles.find { it.uuid == profileUuid }
            ?.inbounds?.map { it.uuid }?.toSet() ?: emptySet()
        s.copy(selectedInbounds = s.selectedInbounds + toAdd)
    }

    fun deselectAllInbounds(profileUuid: String) = _state.update { s ->
        val toRemove = s.profiles.find { it.uuid == profileUuid }
            ?.inbounds?.map { it.uuid }?.toSet() ?: emptySet()
        s.copy(selectedInbounds = s.selectedInbounds - toRemove)
    }

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
                            members = SquadMember.internalMembers(users, squadUuid),
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(membersLoading = false, membersError = e.toUiText()) } }
        }
    }

    fun submit(onSaved: () -> Unit) {
        if (!_state.value.canSave) return
        _state.update { it.copy(isSaving = true, actionError = null) }
        viewModelScope.launch {
            squadRepo.updateInternalSquadFull(
                UpdateInternalSquadFullRequest(
                    uuid = squadUuid,
                    name = _state.value.name.trim(),
                    inbounds = _state.value.selectedInbounds.toList(),
                )
            ).onSuccess {
                _state.update { it.copy(isSaving = false) }
                onSaved()
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, actionError = e.toUiText()) }
            }
        }
    }
}
