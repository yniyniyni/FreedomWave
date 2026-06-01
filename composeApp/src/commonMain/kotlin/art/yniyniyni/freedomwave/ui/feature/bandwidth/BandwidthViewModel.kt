package art.yniyniyni.freedomwave.ui.feature.bandwidth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.api.dto.BandwidthNodesData
import art.yniyniyni.freedomwave.data.repository.BandwidthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

enum class TimeRange(val days: Int, val label: String) {
    DAYS_7(7, "7d"),
    DAYS_30(30, "30d"),
    DAYS_90(90, "90d");

    fun toDateRange(): Pair<String, String> {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val start = today.minus(DatePeriod(days = days))
        return Pair(start.toString(), today.toString())
    }
}

data class BandwidthUiState(
    val isLoading: Boolean       = false,
    val selectedRange: TimeRange = TimeRange.DAYS_7,
    val data: BandwidthNodesData? = null,
    val error: String?           = null
)

class BandwidthViewModel(private val repo: BandwidthRepository) : ViewModel() {

    private val _state = MutableStateFlow(BandwidthUiState())
    val state: StateFlow<BandwidthUiState> = _state.asStateFlow()

    init { load() }

    fun setRange(range: TimeRange) {
        _state.update { it.copy(selectedRange = range) }
        load()
    }

    fun load() {
        val (start, end) = _state.value.selectedRange.toDateRange()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repo.getNodesStats(start, end)
                .onSuccess { data -> _state.update { it.copy(isLoading = false, data = data) } }
                .onFailure { e   -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
