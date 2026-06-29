package org.freedomwave.ui.feature.nodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.freedomwave.data.repository.NodeRepository
import org.freedomwave.domain.model.Node
import org.freedomwave.util.reorderList
import org.freedomwave.ui.l10n.UiText
import org.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NodesUiState(
    val isLoading: Boolean   = false,
    val nodes: List<Node>    = emptyList(),
    val error: UiText?       = null,
    val actionError: UiText? = null
)

class NodesViewModel(private val nodeRepository: NodeRepository) : ViewModel() {

    private val _state = MutableStateFlow(NodesUiState())
    val state: StateFlow<NodesUiState> = _state.asStateFlow()
    
    private var loadJob: Job? = null

    init { load() }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            nodeRepository.getNodes()
                .onSuccess { nodes -> _state.update { it.copy(isLoading = false, nodes = nodes) } }
                .onFailure { e    -> _state.update { it.copy(isLoading = false, error = e.toUiText()) } }
        }
    }

    fun enableNode(uuid: String)   = action(uuid) { nodeRepository.enableNode(uuid) }
    fun disableNode(uuid: String)  = action(uuid) { nodeRepository.disableNode(uuid) }
    fun restartNode(uuid: String)  = action(uuid) { nodeRepository.restartNode(uuid) }
    fun resetTraffic(uuid: String) = action(uuid) { nodeRepository.resetTraffic(uuid) }

    fun deleteNode(uuid: String) {
        viewModelScope.launch {
            nodeRepository.deleteNode(uuid)
                .onSuccess { _state.update { it.copy(nodes = it.nodes.filterNot { n -> n.uuid == uuid }) } }
                .onFailure { e -> _state.update { it.copy(actionError = e.toUiText()) } }
        }
    }

    fun clearActionError() = _state.update { it.copy(actionError = null) }

    private var preReorderNodes: List<Node>? = null

    fun beginReorder() {
        preReorderNodes = _state.value.nodes
    }

    fun moveNode(from: Int, to: Int) {
        _state.update { it.copy(nodes = reorderList(it.nodes, from, to)) }
    }

    fun commitReorder() {
        val snapshot = preReorderNodes
        preReorderNodes = null
        viewModelScope.launch {
            val orderedUuids = _state.value.nodes.map { it.uuid }
            nodeRepository.reorderNodes(orderedUuids)
                .onFailure { e ->
                    _state.update { s ->
                        s.copy(nodes = snapshot ?: s.nodes, actionError = e.toUiText())
                    }
                }
        }
    }

    private fun action(uuid: String, block: suspend () -> Result<Node>) {
        viewModelScope.launch {
            block()
                .onSuccess { updated -> _state.update { it.copy(nodes = it.nodes.map { n -> if (n.uuid == uuid) updated else n }) } }
                .onFailure { e -> _state.update { it.copy(actionError = e.toUiText()) } }
        }
    }
}
