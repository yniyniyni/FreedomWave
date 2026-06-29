package org.freedomwave.ui.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.freedomwave.data.repository.DashboardRepository
import org.freedomwave.domain.model.DashboardStats
import org.freedomwave.ui.l10n.UiText
import org.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class DashboardUiState(
    val isLoading: Boolean      = false,
    val stats: DashboardStats?  = null,
    val error: UiText?          = null,
    val lastUpdatedAt: Long?    = null
)

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            dashboardRepository.getStats()
                .onSuccess { stats -> _state.update { it.copy(isLoading = false, stats = stats, lastUpdatedAt = Clock.System.now().toEpochMilliseconds()) } }
                .onFailure { e   -> _state.update { it.copy(isLoading = false, error = e.toUiText()) } }
        }
    }
}
