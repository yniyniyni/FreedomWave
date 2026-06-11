package art.yniyniyni.freedomwave.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.repository.AuthRepository
import art.yniyniyni.freedomwave.data.store.AppPreferences
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.error_fill_all_fields
import art.yniyniyni.freedomwave.ui.l10n.UiText
import art.yniyniyni.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val showChangeKeyDialog: Boolean = false,
    val dialogServerUrl: String     = "",
    val dialogApiKey: String        = "",
    val dialogIsLoading: Boolean    = false,
    val dialogError: UiText?        = null
)

class SettingsViewModel(
    private val authRepo: AuthRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun openChangeKeyDialog() {
        viewModelScope.launch {
            _state.update { it.copy(
                showChangeKeyDialog = true,
                dialogServerUrl = prefs.getServerUrl(),
                dialogApiKey = "",
                dialogError = null
            ) }
        }
    }

    fun dismissChangeKeyDialog() = _state.update {
        it.copy(showChangeKeyDialog = false, dialogError = null)
    }

    fun onDialogServerUrlChange(v: String) = _state.update { it.copy(dialogServerUrl = v, dialogError = null) }
    fun onDialogApiKeyChange(v: String)    = _state.update { it.copy(dialogApiKey = v, dialogError = null) }

    fun saveApiKey() {
        val s = _state.value
        if (s.dialogServerUrl.isBlank() || s.dialogApiKey.isBlank()) {
            _state.update { it.copy(dialogError = UiText.Res(Res.string.error_fill_all_fields)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(dialogIsLoading = true, dialogError = null) }
            authRepo.saveApiKey(s.dialogServerUrl.trim(), s.dialogApiKey.trim())
                .onSuccess { _state.update { it.copy(dialogIsLoading = false, showChangeKeyDialog = false) } }
                .onFailure { e -> _state.update { it.copy(dialogIsLoading = false, dialogError = e.toUiText()) } }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.saveThemeMode(mode) }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.saveBiometricEnabled(enabled) }
    }

    fun logout() {
        // Route through the repository so the Bearer-token cache is cleared too (one place).
        viewModelScope.launch { authRepo.logout() }
    }
}
