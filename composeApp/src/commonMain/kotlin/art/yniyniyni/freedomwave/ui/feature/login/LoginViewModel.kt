package art.yniyniyni.freedomwave.ui.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.repository.AuthRepository
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.error_fill_all_fields
import art.yniyniyni.freedomwave.ui.l10n.UiText
import art.yniyniyni.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val serverUrl: String  = "",
    val apiKey: String     = "",
    val isLoading: Boolean = false,
    val error: UiText?     = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onServerUrlChange(value: String) = _state.update { it.copy(serverUrl = value, error = null) }
    fun onApiKeyChange(value: String)    = _state.update { it.copy(apiKey = value, error = null) }

    fun save() {
        val s = _state.value
        if (s.serverUrl.isBlank() || s.apiKey.isBlank()) {
            _state.update { it.copy(error = UiText.Res(Res.string.error_fill_all_fields)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.saveApiKey(s.serverUrl.trim(), s.apiKey.trim())
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.toUiText()) }
                }
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }
}
