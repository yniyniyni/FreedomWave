package art.yniyniyni.freedomwave.ui.feature.squads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.repository.SquadRepository
import art.yniyniyni.freedomwave.domain.model.Squad
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.squads_name_required
import art.yniyniyni.freedomwave.ui.l10n.UiText
import art.yniyniyni.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SquadsUiState(
    val activeTab: Int = 0,
    val internalSquads: List<Squad> = emptyList(),
    val externalSquads: List<Squad> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val actionInProgress: Boolean = false,
    val actionError: UiText? = null,
    val showCreateDialog: Boolean = false,
    val dialogName: String = "",
    val dialogIsLoading: Boolean = false,
    val dialogError: UiText? = null
)

class SquadsViewModel(private val repository: SquadRepository) : ViewModel() {

    private val _state = MutableStateFlow(SquadsUiState())
    val state: StateFlow<SquadsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                coroutineScope {
                    val internal = async { repository.getInternalSquads() }
                    val external = async { repository.getExternalSquads() }
                    internal.await() to external.await()
                }
            }.onSuccess { (internalResult, externalResult) ->
                _state.update { s ->
                    s.copy(
                        isLoading = false,
                        internalSquads = internalResult.getOrDefault(emptyList()),
                        externalSquads = externalResult.getOrDefault(emptyList()),
                        error = (internalResult.exceptionOrNull() ?: externalResult.exceptionOrNull())?.toUiText()
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.toUiText()) }
            }
        }
    }

    fun setTab(index: Int) = _state.update { it.copy(activeTab = index) }
    fun clearActionError() = _state.update { it.copy(actionError = null) }

    fun openCreateDialog() = _state.update { it.copy(showCreateDialog = true, dialogName = "", dialogError = null) }
    fun dismissDialog() = _state.update { it.copy(showCreateDialog = false, dialogError = null) }
    fun onDialogNameChange(v: String) = _state.update { it.copy(dialogName = v, dialogError = null) }

    fun createSquad() {
        val s = _state.value
        if (s.dialogName.isBlank()) { _state.update { it.copy(dialogError = UiText.Res(Res.string.squads_name_required)) }; return }
        createSquadInternal(s.dialogName.trim(), isInternal = s.activeTab == 0)
    }

    private fun createSquadInternal(name: String, isInternal: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(dialogIsLoading = true, dialogError = null) }
            val result = if (isInternal) repository.createInternalSquad(name)
                         else repository.createExternalSquad(name)
            result
                .onSuccess { created ->
                    _state.update { st ->
                        if (isInternal) st.copy(dialogIsLoading = false, showCreateDialog = false,
                            internalSquads = st.internalSquads + created)
                        else st.copy(dialogIsLoading = false, showCreateDialog = false,
                            externalSquads = st.externalSquads + created)
                    }
                }
                .onFailure { e -> _state.update { it.copy(dialogIsLoading = false, dialogError = e.toUiText()) } }
        }
    }

    fun deleteSquad(squad: Squad) {
        viewModelScope.launch {
            _state.update { it.copy(actionInProgress = true) }
            val result = if (squad.type == Squad.Type.INTERNAL) repository.deleteInternalSquad(squad.uuid)
                         else repository.deleteExternalSquad(squad.uuid)
            result
                .onSuccess {
                    _state.update { s ->
                        if (squad.type == Squad.Type.INTERNAL)
                            s.copy(actionInProgress = false,
                                internalSquads = s.internalSquads.filter { it.uuid != squad.uuid })
                        else
                            s.copy(actionInProgress = false,
                                externalSquads = s.externalSquads.filter { it.uuid != squad.uuid })
                    }
                }
                .onFailure { e -> _state.update { it.copy(actionInProgress = false, actionError = e.toUiText()) } }
        }
    }
}
