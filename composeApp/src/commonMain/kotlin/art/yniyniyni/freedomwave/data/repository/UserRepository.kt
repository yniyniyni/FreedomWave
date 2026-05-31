package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.ApiError
import art.yniyniyni.freedomwave.data.api.dto.CreateUserRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateUserRequest
import art.yniyniyni.freedomwave.data.api.service.UserService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.User

class UserRepository(
    private val service: UserService,
    private val prefs: AppPreferences
) {
    suspend fun getUsers(): Result<List<User>> = api {
        service.getUsers(prefs.getServerUrl()).response.users.map { User.from(it) }
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
