package art.yniyniyni.freedomwave.ui.feature.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import art.yniyniyni.freedomwave.data.api.dto.CreateUserRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateUserRequest
import art.yniyniyni.freedomwave.data.repository.UserRepository
import art.yniyniyni.freedomwave.domain.model.User
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
    val actionError: String? = null,
    // create / edit form
    val formVisible: Boolean  = false,
    val editingUser: User?    = null,
    val formUsername: String  = "",
    val formTrafficGb: String = "0",
    val formStrategy: String  = "NO_RESET",
    val formExpireDate: String = "",
    val formTag: String        = "",
    val formDescription: String = "",
    val formEmail: String      = "",
    val formIsLoading: Boolean = false,
    val formError: String?     = null,
) {
    val filtered: List<User> get() =
        if (query.isBlank()) users
        else users.filter {
            it.username.contains(query, ignoreCase = true) ||
            it.tag?.contains(query, ignoreCase = true) == true
        }
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

    // Form
    fun openCreateForm() = _state.update {
        it.copy(
            formVisible = true, editingUser = null,
            formUsername = "", formTrafficGb = "0", formStrategy = "NO_RESET",
            formExpireDate = "", formTag = "", formDescription = "", formEmail = "",
            formIsLoading = false, formError = null
        )
    }

    fun openEditForm(user: User) = _state.update {
        val trafficGb = if (user.trafficLimitBytes == 0L) "0"
            else String.format("%.2f", user.trafficLimitBytes.toDouble() / (1024.0 * 1024.0 * 1024.0))
        it.copy(
            formVisible = true, editingUser = user,
            formUsername = user.username,
            formTrafficGb = trafficGb,
            formStrategy = user.trafficLimitStrategy,
            formExpireDate = if (user.expireAt.length >= 10) user.expireAt.take(10) else "",
            formTag = user.tag ?: "",
            formDescription = user.description ?: "",
            formEmail = user.email ?: "",
            formIsLoading = false, formError = null
        )
    }

    fun closeForm() = _state.update { it.copy(formVisible = false, formError = null) }

    fun onFormUsername(v: String)    = _state.update { it.copy(formUsername = v, formError = null) }
    fun onFormTrafficGb(v: String)   = _state.update { it.copy(formTrafficGb = v, formError = null) }
    fun onFormStrategy(v: String)    = _state.update { it.copy(formStrategy = v) }
    fun onFormExpireDate(v: String)  = _state.update { it.copy(formExpireDate = v, formError = null) }
    fun onFormTag(v: String)         = _state.update { it.copy(formTag = v) }
    fun onFormDescription(v: String) = _state.update { it.copy(formDescription = v) }
    fun onFormEmail(v: String)       = _state.update { it.copy(formEmail = v) }

    fun saveForm() {
        val s = _state.value
        val isCreate = s.editingUser == null

        if (isCreate && s.formUsername.isBlank()) {
            _state.update { it.copy(formError = "Username is required") }
            return
        }

        val trafficBytes = s.formTrafficGb.trim().toDoubleOrNull()
        if (trafficBytes == null || trafficBytes < 0) {
            _state.update { it.copy(formError = "Traffic limit must be a number ≥ 0") }
            return
        }
        val trafficLimitBytes = (trafficBytes * 1024.0 * 1024.0 * 1024.0).toLong()

        val expireDate = s.formExpireDate.trim()
        val expireAt: String? = when {
            expireDate.isBlank() && isCreate -> null
            expireDate.isBlank()             -> s.editingUser?.expireAt  // keep existing
            else                             -> "${expireDate}T00:00:00.000Z"
        }

        viewModelScope.launch {
            _state.update { it.copy(formIsLoading = true, formError = null) }
            if (isCreate) {
                val req = CreateUserRequest(
                    username             = s.formUsername.trim(),
                    trafficLimitBytes    = trafficLimitBytes,
                    trafficLimitStrategy = s.formStrategy,
                    expireAt             = expireAt,
                    tag                  = s.formTag.trim().takeIf { it.isNotBlank() },
                    description          = s.formDescription.trim().takeIf { it.isNotBlank() },
                    email                = s.formEmail.trim().takeIf { it.isNotBlank() }
                )
                userRepository.createUser(req)
                    .onSuccess { created ->
                        _state.update { it.copy(
                            formIsLoading = false, formVisible = false,
                            users = listOf(created) + it.users
                        ) }
                    }
                    .onFailure { e -> _state.update { it.copy(formIsLoading = false, formError = e.message) } }
            } else {
                val req = UpdateUserRequest(
                    uuid                 = s.editingUser!!.uuid,
                    trafficLimitBytes    = trafficLimitBytes,
                    trafficLimitStrategy = s.formStrategy,
                    expireAt             = expireAt,
                    tag                  = s.formTag.trim().takeIf { it.isNotBlank() },
                    description          = s.formDescription.trim().takeIf { it.isNotBlank() },
                    email                = s.formEmail.trim().takeIf { it.isNotBlank() }
                )
                userRepository.updateUser(req)
                    .onSuccess { updated ->
                        _state.update { it.copy(
                            formIsLoading = false, formVisible = false,
                            users = it.users.map { u -> if (u.uuid == updated.uuid) updated else u }
                        ) }
                    }
                    .onFailure { e -> _state.update { it.copy(formIsLoading = false, formError = e.message) } }
            }
        }
    }

    private fun action(uuid: String, block: suspend () -> Result<User>) {
        viewModelScope.launch {
            block()
                .onSuccess { updated ->
                    _state.update { it.copy(users = it.users.map { u -> if (u.uuid == uuid) updated else u }) }
                }
                .onFailure { e -> _state.update { it.copy(actionError = e.message) } }
        }
    }
}
