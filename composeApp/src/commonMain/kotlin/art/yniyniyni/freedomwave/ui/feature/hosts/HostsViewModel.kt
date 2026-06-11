package art.yniyniyni.freedomwave.ui.feature.hosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.repository.HostRepository
import art.yniyniyni.freedomwave.domain.model.Host
import art.yniyniyni.freedomwave.ui.l10n.UiText
import art.yniyniyni.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HostsUiState(
    val isLoading: Boolean = false,
    val hosts: List<Host> = emptyList(),
    val error: UiText? = null,
    val actionError: UiText? = null,
    val selected: Host? = null,
    val actionInProgress: Boolean = false
)

class HostsViewModel(private val repo: HostRepository) : ViewModel() {

    private val _state = MutableStateFlow(HostsUiState())
    val state: StateFlow<HostsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repo.getHosts()
                .onSuccess { hosts -> _state.update { it.copy(isLoading = false, hosts = hosts) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.toUiText()) } }
        }
    }

    fun select(host: Host) = _state.update { it.copy(selected = host) }
    fun clearSelection() = _state.update { it.copy(selected = null) }
    fun clearActionError() = _state.update { it.copy(actionError = null) }

    fun toggleEnabled(host: Host) {
        viewModelScope.launch {
            _state.update { it.copy(actionInProgress = true) }
            val result = if (host.isDisabled) repo.enableHost(host.uuid)
                         else repo.disableHost(host.uuid)
            result
                .onSuccess { updatedList ->
                    _state.update { s ->
                        val merged = s.hosts.map { h ->
                            updatedList.firstOrNull { it.uuid == h.uuid } ?: h
                        }
                        val updatedSelected = updatedList.firstOrNull { it.uuid == host.uuid }
                        s.copy(actionInProgress = false, hosts = merged, selected = updatedSelected ?: s.selected)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(actionInProgress = false, actionError = e.toUiText()) }
                }
        }
    }

    fun delete(host: Host) {
        viewModelScope.launch {
            _state.update { it.copy(actionInProgress = true) }
            repo.deleteHost(host.uuid)
                .onSuccess {
                    _state.update { s ->
                        s.copy(
                            actionInProgress = false,
                            hosts = s.hosts.filter { it.uuid != host.uuid },
                            selected = if (s.selected?.uuid == host.uuid) null else s.selected
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(actionInProgress = false, actionError = e.toUiText()) }
                }
        }
    }
}
