package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.ApiError
import art.yniyniyni.freedomwave.data.api.dto.CreateUserRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateUserRequest
import art.yniyniyni.freedomwave.data.api.dto.UserDto
import art.yniyniyni.freedomwave.data.api.dto.UserListData
import art.yniyniyni.freedomwave.data.api.service.UserService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.User

private const val USERS_PAGE_SIZE = 500

/**
 * Page through the user list until [UserListData.total] is reached. The panel caps a
 * single response at the page size, so a one-shot fetch silently truncates large panels.
 * The empty-page guard stops the loop if `total` ever overshoots the real count.
 */
internal suspend fun collectAllUsers(
    pageSize: Int = USERS_PAGE_SIZE,
    fetchPage: suspend (start: Int, size: Int) -> UserListData
): List<UserDto> {
    val first = fetchPage(0, pageSize)
    val all = first.users.toMutableList()
    while (all.size < first.total) {
        val page = fetchPage(all.size, pageSize)
        if (page.users.isEmpty()) break
        all += page.users
    }
    return all
}

class UserRepository(
    private val service: UserService,
    private val prefs: AppPreferences
) {
    suspend fun getUsers(): Result<List<User>> = api {
        val serverUrl = prefs.getServerUrl()
        collectAllUsers { start, size -> service.getUsers(serverUrl, start, size).response }
            .map { User.from(it) }
    }

    suspend fun getUser(uuid: String): Result<User> = api {
        User.from(service.getUser(prefs.getServerUrl(), uuid).response)
    }

    suspend fun createUser(request: CreateUserRequest): Result<User> = api {
        User.from(service.createUser(prefs.getServerUrl(), request).response)
    }

    suspend fun updateUser(request: UpdateUserRequest): Result<User> = api {
        User.from(service.updateUser(prefs.getServerUrl(), request).response)
    }

    suspend fun deleteUser(uuid: String): Result<Unit> = api {
        service.deleteUser(prefs.getServerUrl(), uuid)
    }

    suspend fun enableUser(uuid: String): Result<User> = api {
        User.from(service.enableUser(prefs.getServerUrl(), uuid).response)
    }

    suspend fun disableUser(uuid: String): Result<User> = api {
        User.from(service.disableUser(prefs.getServerUrl(), uuid).response)
    }

    suspend fun resetTraffic(uuid: String): Result<User> = api {
        User.from(service.resetTraffic(prefs.getServerUrl(), uuid).response)
    }

    suspend fun revokeSubscription(uuid: String): Result<User> = api {
        User.from(service.revokeSubscription(prefs.getServerUrl(), uuid).response)
    }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.also { result ->
            if (result.exceptionOrNull() is ApiError.Unauthorized) prefs.clearCredentials()
        }
}
