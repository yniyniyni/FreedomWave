package org.freedomwave.data.repository

import org.freedomwave.data.api.dto.CreateUserRequest
import org.freedomwave.data.api.dto.UpdateUserRequest
import org.freedomwave.data.api.dto.UserDto
import org.freedomwave.data.api.dto.UserListData
import org.freedomwave.data.api.service.UserService
import org.freedomwave.data.store.AppPreferences
import org.freedomwave.domain.model.User

private const val USERS_PAGE_SIZE = 500

/**
 * Page through the user list until [UserListData.total] is reached. The panel caps a
 * single response at the page size, so a one-shot fetch silently truncates large panels.
 *
 * Protections against corrupted backend responses:
 * - Deduplicates by [UserDto.id] so overlapping pages don't produce duplicates. Keyed on `id`
 *   rather than `uuid` because panel 3.x stopped sending uuids — every user would otherwise
 *   dedupe to the same null and the list would collapse to one row.
 * - Caps iterations at [maxIterations] to guard against an inflated `total`.
 * - Breaks on an empty page even when `total` claims more records exist.
 */
internal suspend fun collectAllUsers(
    pageSize: Int = USERS_PAGE_SIZE,
    maxIterations: Int = 100,
    fetchPage: suspend (start: Int, size: Int) -> UserListData
): List<UserDto> {
    val first = fetchPage(0, pageSize)
    val seenIds = mutableSetOf<Int>()
    val all = mutableListOf<UserDto>()
    var offset = 0

    for (user in first.users) {
        if (seenIds.add(user.id)) all.add(user)
    }
    offset += first.users.size

    var iterations = 0
    while (all.size < first.total && iterations < maxIterations) {
        val page = fetchPage(offset, pageSize)
        if (page.users.isEmpty()) break

        for (user in page.users) {
            if (seenIds.add(user.id)) all.add(user)
        }
        offset += page.users.size
        iterations++
    }

    return all
}

/**
 * Point an update body at [userRef], filling `id` on panel 3.x or `uuid` on 2.8.x.
 *
 * The choice is made from the ref's shape rather than a stored panel version, which keeps it
 * consistent with however `User.userRef` was produced: that is `uuid ?: id.toString()`, so a
 * numeric ref can only have come from a 3.x payload and a non-numeric one from a uuid. Any
 * identifier already on the request is replaced — the panel rejects a body naming both.
 */
internal fun UpdateUserRequest.bindUserRef(userRef: String): UpdateUserRequest =
    userRef.toIntOrNull()
        ?.let { copy(id = it, uuid = null) }
        ?: copy(uuid = userRef, id = null)

class UserRepository(
    private val service: UserService,
    private val prefs: AppPreferences
) {
    suspend fun getUsers(): Result<List<User>> = api {
        collectAllUsers { start, size -> service.getUsers(start, size).response }
            .map { User.from(it) }
    }

    suspend fun getUser(userRef: String): Result<User> = api {
        User.from(service.getUser(userRef).response)
    }

    suspend fun createUser(request: CreateUserRequest): Result<User> = api {
        User.from(service.createUser(request).response)
    }

    /** [changes] carries only the edited fields; the identifier is bound here from [userRef]. */
    suspend fun updateUser(userRef: String, changes: UpdateUserRequest): Result<User> = api {
        User.from(service.updateUser(changes.bindUserRef(userRef)).response)
    }

    suspend fun deleteUser(userRef: String): Result<Unit> = api {
        service.deleteUser(userRef)
    }

    suspend fun enableUser(userRef: String): Result<User> = api {
        User.from(service.enableUser(userRef).response)
    }

    suspend fun disableUser(userRef: String): Result<User> = api {
        User.from(service.disableUser(userRef).response)
    }

    suspend fun resetTraffic(userRef: String): Result<User> = api {
        User.from(service.resetTraffic(userRef).response)
    }

    suspend fun revokeSubscription(userRef: String): Result<User> = api {
        User.from(service.revokeSubscription(userRef).response)
    }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.clearOnUnauthorized(prefs)
}
