package org.freedomwave.data.repository

import org.freedomwave.data.api.dto.UserDto
import org.freedomwave.data.api.dto.UserListData
import org.freedomwave.data.api.dto.UserTrafficDto
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectAllUsersTest {

    private fun user(i: Int): UserDto = UserDto(
        uuid = "uuid-$i",
        id = i,
        shortUuid = "short-$i",
        username = "user-$i",
        status = "ACTIVE",
        trafficLimitBytes = 0,
        trafficLimitStrategy = "NO_RESET",
        expireAt = "2030-01-01T00:00:00.000Z",
        trojanPassword = "t",
        vlessUuid = "v",
        ssPassword = "s",
        createdAt = "2024-01-01T00:00:00.000Z",
        updatedAt = "2024-01-01T00:00:00.000Z",
        userTraffic = UserTrafficDto(usedTrafficBytes = 0, lifetimeUsedTrafficBytes = 0),
    )

    /** Builds a paginated source of [total] users that honours start/size like the panel. */
    private fun pagedSource(total: Int, reportedTotal: Int = total): suspend (Int, Int) -> UserListData {
        val all = (0 until total).map { user(it) }
        return { start, size ->
            UserListData(
                users = if (start >= all.size) emptyList() else all.subList(start, minOf(start + size, all.size)),
                total = reportedTotal,
            )
        }
    }

    @Test
    fun `loops over multiple pages until total is reached`() = runBlocking {
        val calls = mutableListOf<Pair<Int, Int>>()
        val source = pagedSource(total = 1200)
        val result = collectAllUsers(pageSize = 500) { start, size ->
            calls += start to size
            source(start, size)
        }
        assertEquals(1200, result.size)
        assertEquals(listOf(0 to 500, 500 to 500, 1000 to 500), calls)
    }

    @Test
    fun `single page when total fits in one fetch`() = runBlocking {
        val calls = mutableListOf<Pair<Int, Int>>()
        val source = pagedSource(total = 3)
        val result = collectAllUsers(pageSize = 500) { start, size ->
            calls += start to size
            source(start, size)
        }
        assertEquals(3, result.size)
        assertEquals(1, calls.size)
    }

    @Test
    fun `empty page stops the loop when total overshoots`() = runBlocking {
        // Server claims 5000 users but only has 500, so page two comes back empty.
        val source = pagedSource(total = 500, reportedTotal = 5000)
        val result = collectAllUsers(pageSize = 500, fetchPage = source)
        assertEquals(500, result.size)
    }
}
