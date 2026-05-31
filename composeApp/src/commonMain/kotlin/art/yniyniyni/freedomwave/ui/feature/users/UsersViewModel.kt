package art.yniyniyni.freedomwave.ui.feature.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.repository.UserRepository
import art.yniyniyni.freedomwave.domain.model.User
import art.yniyniyni.freedomwave.domain.model.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsersUiState(
    val isLoading: Boolean   = false,
    val users: List<User>    = emptyList(),
    val query: String        = "",
    val error: String?       = null,
    val actionError: String? = null
) {
    val filtered: List<User> get() =
        if (query.isBlank()) users
        else users.filter { it.username.contains(query, ignoreCase = true) || it.tag?.contains(query, ignoreCase = true) == true }
}

class UsersViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow(UsersUiState())
    val state: StateFlow<UsersUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            userRepository.getUsers()
                .onSuccess { users -> _state.update { it.copy(isLoading = false, users = users) } }
                .onFailure { e    -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun onQueryChange(q: String) = _state.update { it.copy(query = q) }

    fun enableUser(uuid: String)   = action(uuid) { userRepository.enableUser(uuid) }
    fun disableUser(uuid: String)  = action(uuid) { userRepository.disableUser(uuid) }
    fun resetTraffic(uuid: String) = action(uuid) { userRepository.resetTraffic(uuid) }
    fun deleteUser(uuid: String) {
        viewModelScope.launch {
            userRepository.deleteUser(uuid)
                .onSuccess { _state.update { it.copy(users = it.users.filterNot { u -> u.uuid == uuid }) } }
                .onFailure { e -> _state.update { it.copy(actionError = e.message) } }
        }
    }

    fun clearActionError() = _state.update { it.copy(actionError = null) }

    private fun action(uuid: String, block: suspend () -> Result<User>) {
        viewModelScope.launch {
            block()
                .onSuccess { updated -> _state.update { it.copy(users = it.users.map { u -> if (u.uuid == uuid) updated else u }) } }
                .onFailure { e -> _state.update { it.copy(actionError = e.message) } }
        }
    }
}
