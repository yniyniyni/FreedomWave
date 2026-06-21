package art.yniyniyni.freedomwave.data.repository

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
 *
 * Protections against corrupted backend responses:
 * - Deduplicates by [UserDto.uuid] so overlapping pages don't produce duplicates.
 * - Caps iterations at [maxIterations] to guard against an inflated `total`.
 * - Breaks on an empty page even when `total` claims more records exist.
 * - Logs a warning when collected count diverges from the advertised `total`.
 */
internal suspend fun collectAllUsers(
    pageSize: Int = USERS_PAGE_SIZE,
    maxIterations: Int = 100,
    fetchPage: suspend (start: Int, size: Int) -> UserListData
): List<UserDto> {
    val first = fetchPage(0, pageSize)
    val seenUuids = mutableSetOf<String>()
    val all = mutableListOf<UserDto>()
    var offset = 0

    for (user in first.users) {
        if (seenUuids.add(user.uuid)) all.add(user)
    }
    offset += first.users.size

    var iterations = 0
    while (all.size < first.total && iterations < maxIterations) {
        val page = fetchPage(offset, pageSize)
        if (page.users.isEmpty()) break

        for (user in page.users) {
            if (seenUuids.add(user.uuid)) all.add(user)
        }
        offset += page.users.size
        iterations++
    }

    if (iterations >= maxIterations) {
        println("[UserRepository] collectAllUsers: reached maxIterations ($maxIterations) — total may be corrupted (advertised: ${first.total})")
    }

    if (all.size != first.total) {
        println("[UserRepository] collectAllUsers: collected ${all.size} users but total was ${first.total}")
    }

    return all
}

class UserRepository(
    private val service: UserService,
    private val prefs: AppPreferences
) {
    suspend fun getUsers(): Result<List<User>> = api {
        collectAllUsers { start, size -> service.getUsers(start, size).response }
            .map { User.from(it) }
    }

    suspend fun getUser(uuid: String): Result<User> = api {
        User.from(service.getUser(uuid).response)
    }

    suspend fun createUser(request: CreateUserRequest): Result<User> = api {
        User.from(service.createUser(request).response)
    }

    suspend fun updateUser(request: UpdateUserRequest): Result<User> = api {
        User.from(service.updateUser(request).response)
    }

    suspend fun deleteUser(uuid: String): Result<Unit> = api {
        service.deleteUser(uuid)
    }

    suspend fun enableUser(uuid: String): Result<User> = api {
        User.from(service.enableUser(uuid).response)
    }

    suspend fun disableUser(uuid: String): Result<User> = api {
        User.from(service.disableUser(uuid).response)
    }

    suspend fun resetTraffic(uuid: String): Result<User> = api {
        User.from(service.resetTraffic(uuid).response)
    }

    suspend fun revokeSubscription(uuid: String): Result<User> = api {
        User.from(service.revokeSubscription(uuid).response)
    }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.also { it.clearOnUnauthorized(prefs) }
}
