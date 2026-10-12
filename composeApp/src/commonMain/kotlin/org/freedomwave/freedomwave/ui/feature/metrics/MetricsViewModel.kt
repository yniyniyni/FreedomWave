package org.freedomwave.ui.feature.metrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.freedomwave.data.api.dto.BandwidthNodesData
import org.freedomwave.data.repository.BandwidthRepository
import org.freedomwave.data.repository.MetricsRepository
import org.freedomwave.data.repository.NodeRepository
import org.freedomwave.domain.model.Node
import org.freedomwave.domain.model.NodeMetrics
import org.freedomwave.ui.feature.bandwidth.TimeRange
import org.freedomwave.ui.l10n.UiText
import org.freedomwave.ui.l10n.toUiText

data class MetricsUiState(
    val isLoading: Boolean            = false,
    val isBandwidthLoading: Boolean   = false,
    val selectedRange: TimeRange      = TimeRange.DAYS_7,
    /** Stats for [selectedRange]; null while that range is loading or after it failed. */
    val bandwidth: BandwidthNodesData? = null,
    val bandwidthError: UiText?       = null,
    val nodes: List<Node>             = emptyList(),
    val nodeMetrics: List<NodeMetrics> = emptyList(),
    /** When [nodes] (and so the live interface rates) were last fetched. */
    val liveUpdatedAt: Instant?       = null,
    val error: UiText?                = null,
)

class MetricsViewModel(
    private val nodeRepo: NodeRepository,
    private val bandwidthRepo: BandwidthRepository,
    private val metricsRepo: MetricsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MetricsUiState())
    val state: StateFlow<MetricsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val nodes     = async { nodeRepo.getNodes() }
            val metrics   = async { metricsRepo.getNodesMetrics() }
            val bandwidth = async { loadBandwidth(_state.value.selectedRange) }
            val nodesResult   = nodes.await()
            val metricsResult = metrics.await()
            bandwidth.await()
            _state.update { s ->
                s.copy(
                    isLoading     = false,
                    nodes         = nodesResult.getOrDefault(s.nodes),
                    liveUpdatedAt = if (nodesResult.isSuccess) Clock.System.now() else s.liveUpdatedAt,
                    nodeMetrics   = metricsResult.getOrDefault(s.nodeMetrics),
                    error         = (nodesResult.exceptionOrNull() ?: metricsResult.exceptionOrNull())
                        ?.toUiText() ?: s.error,
                )
            }
        }
    }

    fun setRange(range: TimeRange) {
        if (range == _state.value.selectedRange) return
        // Drop the previous range's data so the card can't show it under the new range's title.
        _state.update { it.copy(selectedRange = range, bandwidth = null, bandwidthError = null) }
        viewModelScope.launch { loadBandwidth(range) }
    }

    fun retryBandwidth() {
        viewModelScope.launch { loadBandwidth(_state.value.selectedRange) }
    }

    /**
     * Re-reads nodes for fresh interface rates. Only the cheap node list is polled; the per-tag
     * metrics endpoint fans out to every node, so it refreshes on [load] alone. Failures keep the
     * last values (the "updated … ago" label shows the staleness).
     */
    fun refreshLive() {
        if (_state.value.isLoading) return // a full load is already fetching nodes
        viewModelScope.launch {
            nodeRepo.getNodes().onSuccess { nodes ->
                _state.update { it.copy(nodes = nodes, liveUpdatedAt = Clock.System.now()) }
            }
        }
    }

    private suspend fun loadBandwidth(range: TimeRange) {
        _state.update { it.copy(isBandwidthLoading = true, bandwidthError = null) }
        val (start, end) = range.toDateRange()
        bandwidthRepo.getNodesStats(start, end)
            .onSuccess { data ->
                // Drop a stale response if the user switched range while it was in flight.
                if (_state.value.selectedRange == range) {
                    _state.update { it.copy(isBandwidthLoading = false, bandwidth = data) }
                }
            }
            .onFailure { e ->
                if (_state.value.selectedRange == range) {
                    _state.update { it.copy(isBandwidthLoading = false, bandwidthError = e.toUiText()) }
                }
            }
    }
}
