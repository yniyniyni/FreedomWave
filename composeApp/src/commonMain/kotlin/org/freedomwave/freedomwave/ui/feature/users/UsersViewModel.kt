package org.freedomwave.ui.feature.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.freedomwave.data.api.dto.CreateUserRequest
import org.freedomwave.data.api.dto.UpdateUserRequest
import org.freedomwave.data.repository.NodeRepository
import org.freedomwave.data.repository.SquadRepository
import org.freedomwave.data.repository.UserRepository
import org.freedomwave.domain.model.Node
import org.freedomwave.domain.model.Squad
import org.freedomwave.domain.model.User
import org.freedomwave.domain.model.UserStatus
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.users_device_limit_invalid
import freedomwave.composeapp.generated.resources.users_email_error
import freedomwave.composeapp.generated.resources.users_tag_error
import freedomwave.composeapp.generated.resources.users_traffic_limit_invalid
import freedomwave.composeapp.generated.resources.users_username_error
import org.freedomwave.ui.l10n.UiText
import org.freedomwave.ui.l10n.toUiText
import org.freedomwave.util.FOREVER_DATE
import org.freedomwave.util.ExpiryPreset
import org.freedomwave.util.presetExpiryMillis
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

// Mirror the Remnawave backend create/update constraints so invalid input is caught
// before the network round-trip, with a clear message.
private val USERNAME_REGEX = Regex("^[A-Za-z0-9_-]{3,36}$")
private val TAG_REGEX = Regex("^[A-Z0-9_]{1,16}$")
private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

private val USERNAME_ERROR = UiText.Res(Res.string.users_username_error)
private val TAG_ERROR = UiText.Res(Res.string.users_tag_error)
private val EMAIL_ERROR = UiText.Res(Res.string.users_email_error)

data class UsersUiState(
    val isLoading: Boolean   = false,
    val users: List<User>    = emptyList(),
    val query: String        = "",
    val error: UiText?       = null,
    val actionError: UiText? = null,
    // sort
    val sortField: UserSortField = UserSortField.ID,
    val sortAscending: Boolean   = false,
    // category filter
    val category: UserCategory   = UserCategory.ALL,
    // node map: uuid -> Node (for last-connection display)
    val nodesByUuid: Map<String, Node> = emptyMap(),
    // create / edit form
    val editingUser: User?     = null,
    val formUsername: String   = "",
    val formTrafficGb: String  = "0",
    val formStrategy: String   = "NO_RESET",
    val formStatusEnabled: Boolean = true,
    val formExpireMillis: Long = 0L,
    val formHwid: String       = "",
    val formTag: String        = "",
    val formDescription: String = "",
    val formEmail: String      = "",
    val formSquads: List<Squad> = emptyList(),
    val formSelectedSquadUuids: Set<String> = emptySet(),
    val formIsLoading: Boolean = false,
    val formError: UiText?     = null,
) {
    val visible: List<User> get() {
        val byQuery = if (query.isBlank()) users
            else users.filter {
                it.username.contains(query, ignoreCase = true) ||
                it.tag?.contains(query, ignoreCase = true) == true
            }
        val matched = if (category == UserCategory.ALL) byQuery
            else byQuery.filter { it.matchesCategory(category) }
        return sortedUsers(matched, sortField, sortAscending)
    }

    // Live per-field validation surfaced under each input. Only flags a field once the user
    // has typed something invalid (empty fields stay clean; saveForm() is the final gate).
    val usernameError: UiText? get() =
        if (editingUser == null && formUsername.isNotBlank() && !USERNAME_REGEX.matches(formUsername.trim()))
            USERNAME_ERROR else null
    val tagError: UiText? get() =
        if (formTag.isNotBlank() && !TAG_REGEX.matches(formTag.trim())) TAG_ERROR else null
    val emailError: UiText? get() =
        if (formEmail.isNotBlank() && !EMAIL_REGEX.matches(formEmail.trim())) EMAIL_ERROR else null
}

class UsersViewModel(
    private val userRepository: UserRepository,
    private val nodeRepository: NodeRepository,
    private val squadRepository: SquadRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UsersUiState())
    val state: StateFlow<UsersUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            userRepository.getUsers()
                .onSuccess { users -> _state.update { it.copy(isLoading = false, users = users) } }
                .onFailure { e    -> _state.update { it.copy(isLoading = false, error = e.toUiText()) } }
        }
        viewModelScope.launch {
            nodeRepository.getNodes()
                .onSuccess { nodes -> _state.update { it.copy(nodesByUuid = nodes.associateBy { n -> n.uuid }) } }
            // node map is non-essential; ignore failures
        }
    }

    fun onQueryChange(q: String) = _state.update { it.copy(query = q) }

    fun onCategorySelected(category: UserCategory) = _state.update { it.copy(category = category) }

    fun onSortSelected(field: UserSortField) = _state.update {
        if (it.sortField == field) it.copy(sortAscending = !it.sortAscending)
        else it.copy(sortField = field, sortAscending = true)
    }

    fun enableUser(uuid: String)         = action(uuid) { userRepository.enableUser(uuid) }
    fun disableUser(uuid: String)        = action(uuid) { userRepository.disableUser(uuid) }
    fun resetTraffic(uuid: String)       = action(uuid) { userRepository.resetTraffic(uuid) }
    fun revokeSubscription(uuid: String) = action(uuid) { userRepository.revokeSubscription(uuid) }

    /** Apply an updated User returned by the detail VM's quick-edit calls into the shared list. */
    fun applyUserUpdate(updated: User) {
        _state.update { it.copy(users = it.users.map { u -> if (u.uuid == updated.uuid) updated else u }) }
    }

    fun deleteUser(uuid: String) {
        viewModelScope.launch {
            userRepository.deleteUser(uuid)
                .onSuccess { _state.update { it.copy(users = it.users.filterNot { u -> u.uuid == uuid }) } }
                .onFailure { e -> _state.update { it.copy(actionError = e.toUiText()) } }
        }
    }

    fun clearActionError() = _state.update { it.copy(actionError = null) }

    // Form open / close
    fun openCreateForm() {
        _state.update {
            it.copy(
                editingUser = null,
                formUsername = "", formTrafficGb = "0", formStrategy = "NO_RESET",
                formStatusEnabled = true,
                formExpireMillis = presetExpiryMillis(ExpiryPreset.MONTH),
                formHwid = "", formTag = "", formDescription = "", formEmail = "",
                formSelectedSquadUuids = emptySet(),
                formIsLoading = false, formError = null,
            )
        }
        loadSquads()
    }

    fun openEditForm(user: User) {
        val trafficGb = if (user.trafficLimitBytes == 0L) "0" else {
            val gb = user.trafficLimitBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val rounded = (gb * 100).roundToLong() / 100.0
            rounded.toString().removeSuffix(".0")
        }
        val expireMillis = Instant.parse(
            if (user.expireAt.isBlank()) FOREVER_DATE else user.expireAt
        ).toEpochMilliseconds()
        _state.update {
            it.copy(
                editingUser = user,
                formUsername = user.username,
                formTrafficGb = trafficGb,
                formStrategy = user.trafficLimitStrategy,
                formStatusEnabled = user.status != UserStatus.DISABLED,
                formExpireMillis = expireMillis,
                formHwid = user.hwidDeviceLimit?.toString() ?: "",
                formTag = user.tag ?: "",
                formDescription = user.description ?: "",
                formEmail = user.email ?: "",
                formSelectedSquadUuids = user.activeSquadUuids.toSet(),
                formIsLoading = false, formError = null,
            )
        }
        loadSquads()
    }

    private fun loadSquads() {
        viewModelScope.launch {
            squadRepository.getInternalSquads()
                .onSuccess { squads -> _state.update { it.copy(formSquads = squads) } }
            // squad list is optional; ignore failures
        }
    }

    // Form field handlers
    fun onFormUsername(v: String)    = _state.update { it.copy(formUsername = v, formError = null) }
    fun onFormTrafficGb(v: String)   = _state.update { it.copy(formTrafficGb = v, formError = null) }
    fun onFormStrategy(v: String)    = _state.update { it.copy(formStrategy = v) }
    fun onFormStatusEnabled(v: Boolean) = _state.update { it.copy(formStatusEnabled = v) }
    fun onFormExpireMillis(v: Long)  = _state.update { it.copy(formExpireMillis = v, formError = null) }
    fun onFormHwid(v: String)        = _state.update { it.copy(formHwid = v, formError = null) }
    fun onFormTag(v: String)         = _state.update { it.copy(formTag = v) }
    fun onFormDescription(v: String) = _state.update { it.copy(formDescription = v) }
    fun onFormEmail(v: String)       = _state.update { it.copy(formEmail = v) }
    fun onFormSquadToggle(uuid: String) = _state.update {
        val sel = it.formSelectedSquadUuids
        it.copy(formSelectedSquadUuids = if (uuid in sel) sel - uuid else sel + uuid)
    }

    fun saveForm(onSuccess: () -> Unit) {
        val s = _state.value
        val isCreate = s.editingUser == null

        // Username is immutable after create, so only validate it on create.
        if (isCreate && !USERNAME_REGEX.matches(s.formUsername.trim())) {
            _state.update { it.copy(formError = USERNAME_ERROR) }
            return
        }

        val tag = s.formTag.trim()
        if (tag.isNotEmpty() && !TAG_REGEX.matches(tag)) {
            _state.update { it.copy(formError = TAG_ERROR) }
            return
        }

        val email = s.formEmail.trim()
        if (email.isNotEmpty() && !EMAIL_REGEX.matches(email)) {
            _state.update { it.copy(formError = EMAIL_ERROR) }
            return
        }

        val trafficGb = s.formTrafficGb.trim().toDoubleOrNull()
        if (trafficGb == null || trafficGb < 0) {
            _state.update { it.copy(formError = UiText.Res(Res.string.users_traffic_limit_invalid)) }
            return
        }
        val trafficLimitBytes = (trafficGb * 1024.0 * 1024.0 * 1024.0).toLong()

        val hwid: Int? = s.formHwid.trim().let { if (it.isBlank()) null else it.toIntOrNull() }
        if (s.formHwid.trim().isNotBlank() && (hwid == null || hwid < 0)) {
            _state.update { it.copy(formError = UiText.Res(Res.string.users_device_limit_invalid)) }
            return
        }

        val expireAt = Instant.fromEpochMilliseconds(s.formExpireMillis).toString()
        val statusForCreate = if (s.formStatusEnabled) "ACTIVE" else "DISABLED"
        val squads = s.formSelectedSquadUuids.toList()

        viewModelScope.launch {
            _state.update { it.copy(formIsLoading = true, formError = null) }
            if (isCreate) {
                val req = CreateUserRequest(
                    username             = s.formUsername.trim(),
                    status               = statusForCreate,
                    trafficLimitBytes    = trafficLimitBytes,
                    trafficLimitStrategy = s.formStrategy,
                    expireAt             = expireAt,
                    tag                  = s.formTag.trim().takeIf { it.isNotBlank() },
                    description          = s.formDescription.trim().takeIf { it.isNotBlank() },
                    email                = s.formEmail.trim().takeIf { it.isNotBlank() },
                    hwidDeviceLimit      = hwid,
                    activeInternalSquads = squads.takeIf { it.isNotEmpty() },
                )
                userRepository.createUser(req)
                    .onSuccess { created ->
                        _state.update { it.copy(formIsLoading = false, users = listOf(created) + it.users) }
                        onSuccess()
                    }
                    .onFailure { e -> _state.update { it.copy(formIsLoading = false, formError = e.toUiText()) } }
            } else {
                val wasEnabled = s.editingUser!!.status != UserStatus.DISABLED
                val statusForUpdate: String? =
                    if (s.formStatusEnabled == wasEnabled) null
                    else if (s.formStatusEnabled) "ACTIVE" else "DISABLED"
                val req = UpdateUserRequest(
                    uuid                 = s.editingUser.uuid,
                    status               = statusForUpdate,
                    trafficLimitBytes    = trafficLimitBytes,
                    trafficLimitStrategy = s.formStrategy,
                    expireAt             = expireAt,
                    tag                  = s.formTag.trim().takeIf { it.isNotBlank() },
                    description          = s.formDescription.trim().takeIf { it.isNotBlank() },
                    email                = s.formEmail.trim().takeIf { it.isNotBlank() },
                    hwidDeviceLimit      = hwid,
                    activeInternalSquads = squads,
                )
                userRepository.updateUser(req)
                    .onSuccess { updated ->
                        _state.update { it.copy(
                            formIsLoading = false,
                            users = it.users.map { u -> if (u.uuid == updated.uuid) updated else u },
                        ) }
                        onSuccess()
                    }
                    .onFailure { e -> _state.update { it.copy(formIsLoading = false, formError = e.toUiText()) } }
            }
        }
    }

    private fun action(uuid: String, block: suspend () -> Result<User>) {
        viewModelScope.launch {
            block()
                .onSuccess { updated ->
                    _state.update { it.copy(users = it.users.map { u -> if (u.uuid == uuid) updated else u }) }
                }
                .onFailure { e -> _state.update { it.copy(actionError = e.toUiText()) } }
        }
    }
}
