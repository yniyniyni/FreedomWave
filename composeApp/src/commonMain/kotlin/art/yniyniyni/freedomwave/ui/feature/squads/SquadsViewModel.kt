package art.yniyniyni.freedomwave.ui.feature.squads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.repository.SquadRepository
import art.yniyniyni.freedomwave.domain.model.Squad
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
    val error: String? = null,
    val selected: Squad? = null,
    val actionInProgress: Boolean = false,
    val actionError: String? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val dialogName: String = "",
    val dialogIsLoading: Boolean = false,
    val dialogError: String? = null
)

class SquadsViewModel(private val repo: SquadRepository) : ViewModel() {

    private val _state = MutableStateFlow(SquadsUiState())
    val state: StateFlow<SquadsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                coroutineScope {
                    val internal = async { repo.getInternalSquads() }
                    val external = async { repo.getExternalSquads() }
                    internal.await() to external.await()
                }
            }.onSuccess { (internalResult, externalResult) ->
                _state.update { s ->
                    s.copy(
                        isLoading = false,
                        internalSquads = internalResult.getOrDefault(emptyList()),
                        externalSquads = externalResult.getOrDefault(emptyList()),
                        error = (internalResult.exceptionOrNull() ?: externalResult.exceptionOrNull())?.message
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setTab(index: Int) = _state.update { it.copy(activeTab = index, selected = null) }
    fun select(squad: Squad) = _state.update { it.copy(selected = squad) }
    fun clearSelection() = _state.update { it.copy(selected = null) }
    fun clearActionError() = _state.update { it.copy(actionError = null) }

    fun openCreateDialog() = _state.update { it.copy(showCreateDialog = true, dialogName = "", dialogError = null) }
    fun openEditDialog(squad: Squad) = _state.update { it.copy(showEditDialog = true, dialogName = squad.name, dialogError = null) }
    fun dismissDialog() = _state.update { it.copy(showCreateDialog = false, showEditDialog = false, dialogError = null) }
    fun onDialogNameChange(v: String) = _state.update { it.copy(dialogName = v, dialogError = null) }

    fun createSquad() {
        val s = _state.value
        if (s.dialogName.isBlank()) { _state.update { it.copy(dialogError = "Name is required") }; return }
        viewModelScope.launch {
            _state.update { it.copy(dialogIsLoading = true, dialogError = null) }
            val result = if (s.activeTab == 0) repo.createInternalSquad(s.dialogName.trim())
                         else repo.createExternalSquad(s.dialogName.trim())
            result
                .onSuccess { created ->
                    _state.update { st ->
                        val list = if (st.activeTab == 0) st.internalSquads + created
                                   else st.externalSquads + created
                        if (st.activeTab == 0) st.copy(dialogIsLoading = false, showCreateDialog = false, internalSquads = list)
                        else st.copy(dialogIsLoading = false, showCreateDialog = false, externalSquads = list)
                    }
                }
                .onFailure { e -> _state.update { it.copy(dialogIsLoading = false, dialogError = e.message) } }
        }
    }

    fun updateSquad() {
        val s = _state.value
        val squad = s.selected ?: return
        if (s.dialogName.isBlank()) { _state.update { it.copy(dialogError = "Name is required") }; return }
        viewModelScope.launch {
            _state.update { it.copy(dialogIsLoading = true, dialogError = null) }
            val result = if (squad.type == Squad.Type.INTERNAL) repo.updateInternalSquad(squad.uuid, s.dialogName.trim())
                         else repo.updateExternalSquad(squad.uuid, s.dialogName.trim())
            result
                .onSuccess { updated ->
                    _state.update { st ->
                        if (squad.type == Squad.Type.INTERNAL)
                            st.copy(dialogIsLoading = false, showEditDialog = false, selected = updated,
                                internalSquads = st.internalSquads.map { if (it.uuid == updated.uuid) updated else it })
                        else
                            st.copy(dialogIsLoading = false, showEditDialog = false, selected = updated,
                                externalSquads = st.externalSquads.map { if (it.uuid == updated.uuid) updated else it })
                    }
                }
                .onFailure { e -> _state.update { it.copy(dialogIsLoading = false, dialogError = e.message) } }
        }
    }

    fun deleteSquad(squad: Squad) {
        viewModelScope.launch {
            _state.update { it.copy(actionInProgress = true) }
            val result = if (squad.type == Squad.Type.INTERNAL) repo.deleteInternalSquad(squad.uuid)
                         else repo.deleteExternalSquad(squad.uuid)
            result
                .onSuccess {
                    _state.update { s ->
                        if (squad.type == Squad.Type.INTERNAL)
                            s.copy(actionInProgress = false, selected = null,
                                internalSquads = s.internalSquads.filter { it.uuid != squad.uuid })
                        else
                            s.copy(actionInProgress = false, selected = null,
                                externalSquads = s.externalSquads.filter { it.uuid != squad.uuid })
                    }
                }
                .onFailure { e -> _state.update { it.copy(actionInProgress = false, actionError = e.message) } }
        }
    }
}
