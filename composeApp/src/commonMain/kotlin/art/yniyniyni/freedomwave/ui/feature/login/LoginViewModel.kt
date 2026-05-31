package art.yniyniyni.freedomwave.ui.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val serverUrl: String  = "",
    val username: String   = "",
    val password: String   = "",
    val isLoading: Boolean = false,
    val error: String?     = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onServerUrlChange(value: String) = _state.update { it.copy(serverUrl = value, error = null) }
    fun onUsernameChange(value: String)  = _state.update { it.copy(username = value, error = null) }
    fun onPasswordChange(value: String)  = _state.update { it.copy(password = value, error = null) }

    fun login() {
        val s = _state.value
        if (s.serverUrl.isBlank() || s.username.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = "Please fill in all fields") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.login(s.serverUrl.trim(), s.username.trim(), s.password)
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Login failed") }
                }
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }
}
