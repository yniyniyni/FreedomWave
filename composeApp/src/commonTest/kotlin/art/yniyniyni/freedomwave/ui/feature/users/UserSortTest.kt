package art.yniyniyni.freedomwave.ui.feature.users

import art.yniyniyni.freedomwave.domain.model.User
import art.yniyniyni.freedomwave.domain.model.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class UserSortTest {
    private fun user(
        id: Int, name: String, status: UserStatus = UserStatus.ACTIVE, onlineAt: String? = null,
    ) = User(
        uuid = "u$id", id = id, shortUuid = "s$id", username = name, status = status,
        trafficLimitBytes = 0, trafficLimitStrategy = "NO_RESET", expireAt = "",
        usedTrafficBytes = 0, lifetimeUsedTrafficBytes = 0, subscriptionUrl = "",
        onlineAt = onlineAt, email = null, tag = null, description = null, telegramId = null,
        hwidDeviceLimit = null, activeSquads = emptyList(), activeSquadUuids = emptyList(),
        lastConnectedNodeUuid = null, createdAt = "",
    )

    private val users = listOf(
        user(3, "charlie", UserStatus.DISABLED, "2026-06-01T10:00:00Z"),
        user(1, "alice", UserStatus.ACTIVE, "2026-06-01T12:00:00Z"),
        user(2, "Bob", UserStatus.EXPIRED, null),
    )

    @Test fun username_ascending_case_insensitive() =
        assertEquals(listOf("alice", "Bob", "charlie"),
            sortedUsers(users, UserSortField.USERNAME, true).map { it.username })

    @Test fun username_descending() =
        assertEquals(listOf("charlie", "Bob", "alice"),
            sortedUsers(users, UserSortField.USERNAME, false).map { it.username })

    @Test fun id_ascending() =
        assertEquals(listOf(1, 2, 3),
            sortedUsers(users, UserSortField.ID, true).map { it.id })

    @Test fun status_ascending_active_first_disabled_last() =
        assertEquals(listOf(UserStatus.ACTIVE, UserStatus.EXPIRED, UserStatus.DISABLED),
            sortedUsers(users, UserSortField.STATUS, true).map { it.status })

    @Test fun online_ascending_nulls_last() =
        assertEquals(listOf("charlie", "alice", "Bob"),
            sortedUsers(users, UserSortField.ONLINE, true).map { it.username })
}
