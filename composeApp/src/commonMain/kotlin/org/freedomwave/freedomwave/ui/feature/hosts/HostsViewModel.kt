package org.freedomwave.ui.feature.hosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.freedomwave.data.repository.HostRepository
import org.freedomwave.domain.model.Host
import org.freedomwave.ui.l10n.UiText
import org.freedomwave.ui.l10n.toUiText
import org.freedomwave.util.reorderList
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
    val actionInProgress: Boolean = false
)

class HostsViewModel(private val repository: HostRepository) : ViewModel() {

    private val _state = MutableStateFlow(HostsUiState())
    val state: StateFlow<HostsUiState> = _state.asStateFlow()

    private var preReorderHosts: List<Host>? = null

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getHosts()
                .onSuccess { hosts -> _state.update { it.copy(isLoading = false, hosts = hosts) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.toUiText()) } }
        }
    }

    fun clearActionError() = _state.update { it.copy(actionError = null) }

    fun delete(host: Host) {
        viewModelScope.launch {
            _state.update { it.copy(actionInProgress = true) }
            repository.deleteHost(host.uuid)
                .onSuccess {
                    _state.update { s ->
                        s.copy(
                            actionInProgress = false,
                            hosts = s.hosts.filter { it.uuid != host.uuid },
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(actionInProgress = false, actionError = e.toUiText()) }
                }
        }
    }

    fun beginReorder() {
        preReorderHosts = _state.value.hosts
    }

    fun moveHost(from: Int, to: Int) {
        _state.update { it.copy(hosts = reorderList(it.hosts, from, to)) }
    }

    fun commitReorder() {
        val snapshot = preReorderHosts
        preReorderHosts = null
        viewModelScope.launch {
            val orderedUuids = _state.value.hosts.map { it.uuid }
            repository.reorderHosts(orderedUuids)
                .onFailure { e ->
                    _state.update { s ->
                        s.copy(
                            hosts = snapshot ?: s.hosts,
                            actionError = e.toUiText(),
                        )
                    }
                }
        }
    }
}
