package art.yniyniyni.freedomwave.ui.feature.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.api.dto.UpdateUserRequest
import art.yniyniyni.freedomwave.data.repository.HwidRepository
import art.yniyniyni.freedomwave.data.repository.SubHistoryRepository
import art.yniyniyni.freedomwave.data.repository.UserRepository
import art.yniyniyni.freedomwave.domain.model.HwidDevice
import art.yniyniyni.freedomwave.domain.model.IpRow
import art.yniyniyni.freedomwave.domain.model.User
import art.yniyniyni.freedomwave.ui.l10n.UiText
import art.yniyniyni.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant
import kotlinx.coroutines.launch

data class UserDetailUiState(
    // Devices section
    val devicesLoading: Boolean        = true,
    val devices: List<HwidDevice>      = emptyList(),
    val devicesError: UiText?          = null,
    val devicesExpanded: Boolean       = true,

    // IP section
    val ipLoading: Boolean             = true,
    val ipRows: List<IpRow>            = emptyList(),
    val ipError: UiText?               = null,
    val ipExpanded: Boolean            = true,

    // Quick-edit action feedback
    val actionError: UiText?           = null,
    val actionSuccess: UiText?         = null,

    // Quick-edit dialogs
    val showSetLimitDialog: Boolean    = false,
    val setLimitGbInput: String        = "",
    val showSetExpiryDialog: Boolean   = false,
    val setExpiryMillis: Long          = 0L,
    val showDeviceLimitDialog: Boolean = false,
    val deviceLimitInput: String       = "",
)

class UserDetailViewModel(
    private val userUuid: String,
    private val userRepository: UserRepository,
    private val hwidRepository: HwidRepository,
    private val subHistoryRepository: SubHistoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UserDetailUiState())
    val state: StateFlow<UserDetailUiState> = _state.asStateFlow()

    init {
        loadDevices()
        loadIpRows()
    }

    fun reload() {
        loadDevices()
        loadIpRows()
    }

    private fun loadDevices() {
        viewModelScope.launch {
            _state.update { it.copy(devicesLoading = true, devicesError = null) }
            hwidRepository.getDevices(userUuid)
                .onSuccess { list -> _state.update { it.copy(devicesLoading = false, devices = list) } }
                .onFailure { e   -> _state.update { it.copy(devicesLoading = false, devicesError = e.toUiText()) } }
        }
    }

    private fun loadIpRows() {
        viewModelScope.launch {
            _state.update { it.copy(ipLoading = true, ipError = null) }
            subHistoryRepository.getIpRows(userUuid)
                .onSuccess { rows -> _state.update { it.copy(ipLoading = false, ipRows = rows) } }
                .onFailure { e   -> _state.update { it.copy(ipLoading = false, ipError = e.toUiText()) } }
        }
    }

    fun toggleDevicesExpanded() = _state.update { it.copy(devicesExpanded = !it.devicesExpanded) }
    fun toggleIpExpanded()      = _state.update { it.copy(ipExpanded = !it.ipExpanded) }

    // ── Quick-edit: Revoke Subscription ──────────────────────────────────────

    fun revokeSubscription(onUpdated: (User) -> Unit) {
        viewModelScope.launch {
            userRepository.revokeSubscription(userUuid)
                .onSuccess { updated ->
                    _state.update { it.copy(actionSuccess = UiText.Raw("Subscription revoked")) }
                    onUpdated(updated)
                }
                .onFailure { e -> _state.update { it.copy(actionError = e.toUiText()) } }
        }
    }

    // ── Quick-edit: Set Traffic Limit ─────────────────────────────────────────

    fun openSetLimitDialog(currentBytes: Long) {
        val gb = if (currentBytes == 0L) "" else {
            val v = currentBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val rounded = (v * 100).toLong() / 100.0
            rounded.toString().removeSuffix(".0")
        }
        _state.update { it.copy(showSetLimitDialog = true, setLimitGbInput = gb) }
    }

    fun onSetLimitInput(v: String) = _state.update { it.copy(setLimitGbInput = v) }
    fun dismissSetLimitDialog()    = _state.update { it.copy(showSetLimitDialog = false) }

    fun confirmSetLimit(onUpdated: (User) -> Unit) {
        val gbStr = _state.value.setLimitGbInput.trim()
        val gb = gbStr.toDoubleOrNull()
        if (gb == null || gb < 0) {
            _state.update { it.copy(actionError = UiText.Raw("Enter a number ≥ 0")) }
            return
        }
        val bytes = (gb * 1024.0 * 1024.0 * 1024.0).toLong()
        _state.update { it.copy(showSetLimitDialog = false) }
        viewModelScope.launch {
            userRepository.updateUser(UpdateUserRequest(uuid = userUuid, trafficLimitBytes = bytes))
                .onSuccess { updated ->
                    _state.update { it.copy(actionSuccess = UiText.Raw("Traffic limit updated")) }
                    onUpdated(updated)
                }
                .onFailure { e -> _state.update { it.copy(actionError = e.toUiText()) } }
        }
    }

    // ── Quick-edit: Set Expiry ────────────────────────────────────────────────

    fun openSetExpiryDialog(currentMillis: Long) =
        _state.update { it.copy(showSetExpiryDialog = true, setExpiryMillis = currentMillis) }

    fun onSetExpiryMillis(v: Long) = _state.update { it.copy(setExpiryMillis = v) }
    fun dismissSetExpiryDialog()   = _state.update { it.copy(showSetExpiryDialog = false) }

    fun confirmSetExpiry(onUpdated: (User) -> Unit) {
        val millis = _state.value.setExpiryMillis
        _state.update { it.copy(showSetExpiryDialog = false) }
        val expireAt = Instant.fromEpochMilliseconds(millis).toString()
        viewModelScope.launch {
            userRepository.updateUser(UpdateUserRequest(uuid = userUuid, expireAt = expireAt))
                .onSuccess { updated ->
                    _state.update { it.copy(actionSuccess = UiText.Raw("Expiry updated")) }
                    onUpdated(updated)
                }
                .onFailure { e -> _state.update { it.copy(actionError = e.toUiText()) } }
        }
    }

    // ── Quick-edit: Device Limit stepper ──────────────────────────────────────

    fun adjustDeviceLimit(current: Int?, delta: Int, onUpdated: (User) -> Unit) {
        val newLimit = ((current ?: 0) + delta).coerceAtLeast(0)
        setDeviceLimit(newLimit, onUpdated)
    }

    fun openDeviceLimitDialog(current: Int?) =
        _state.update { it.copy(showDeviceLimitDialog = true, deviceLimitInput = (current ?: 0).toString()) }

    fun onDeviceLimitInput(v: String) = _state.update { it.copy(deviceLimitInput = v) }
    fun dismissDeviceLimitDialog()    = _state.update { it.copy(showDeviceLimitDialog = false) }

    fun confirmDeviceLimit(onUpdated: (User) -> Unit) {
        val limit = _state.value.deviceLimitInput.trim().toIntOrNull()
        if (limit == null || limit < 0) {
            _state.update { it.copy(actionError = UiText.Raw("Enter a whole number ≥ 0")) }
            return
        }
        _state.update { it.copy(showDeviceLimitDialog = false) }
        setDeviceLimit(limit, onUpdated)
    }

    private fun setDeviceLimit(newLimit: Int, onUpdated: (User) -> Unit) {
        viewModelScope.launch {
            userRepository.updateUser(UpdateUserRequest(uuid = userUuid, hwidDeviceLimit = newLimit))
                .onSuccess { updated ->
                    _state.update { it.copy(actionSuccess = UiText.Raw("Device limit set to $newLimit")) }
                    onUpdated(updated)
                }
                .onFailure { e -> _state.update { it.copy(actionError = e.toUiText()) } }
        }
    }

    fun clearMessages() = _state.update { it.copy(actionError = null, actionSuccess = null) }
}
