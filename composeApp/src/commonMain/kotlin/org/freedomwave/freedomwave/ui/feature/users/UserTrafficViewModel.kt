package org.freedomwave.ui.feature.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.freedomwave.data.api.dto.BandwidthNodesData
import org.freedomwave.data.repository.BandwidthRepository
import org.freedomwave.ui.feature.bandwidth.TimeRange
import org.freedomwave.ui.l10n.UiText
import org.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserTrafficUiState(
    val isLoading: Boolean = false,
    /** Active preset, or null when a custom [customStart]–[customEnd] range is selected. */
    val selectedRange: TimeRange? = TimeRange.DAYS_7,
    val customStart: String? = null,
    val customEnd: String? = null,
    val data: BandwidthNodesData? = null,
    val error: UiText? = null
)

class UserTrafficViewModel(
    private val repo: BandwidthRepository,
    private val userUuid: String
) : ViewModel() {

    private val _state = MutableStateFlow(UserTrafficUiState())
    val state: StateFlow<UserTrafficUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun setRange(range: TimeRange) {
        _state.update { it.copy(selectedRange = range, customStart = null, customEnd = null) }
        load()
    }

    /** Select an arbitrary range; [start]/[end] are ISO `YYYY-MM-DD` dates. */
    fun setCustomRange(start: String, end: String) {
        _state.update { it.copy(selectedRange = null, customStart = start, customEnd = end) }
        load()
    }

    fun load() {
        val s = _state.value
        val (start, end) = if (s.customStart != null && s.customEnd != null) {
            s.customStart to s.customEnd
        } else {
            (s.selectedRange ?: TimeRange.DAYS_7).toDateRange()
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repo.getUserStats(userUuid, start, end)
                .onSuccess { data -> _state.update { it.copy(isLoading = false, data = data) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.toUiText()) } }
        }
    }
}
