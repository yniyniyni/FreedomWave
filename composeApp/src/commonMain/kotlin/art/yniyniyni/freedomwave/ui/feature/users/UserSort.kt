package art.yniyniyni.freedomwave.ui.feature.users

import art.yniyniyni.freedomwave.domain.model.User
import art.yniyniyni.freedomwave.domain.model.UserStatus
import art.yniyniyni.freedomwave.util.parseInstant

enum class UserSortField(val label: String) {
    USERNAME("Username"),
    STATUS("Status"),
    ONLINE("Online"),
    ID("ID"),
}

private fun statusOrder(s: UserStatus) = when (s) {
    UserStatus.ACTIVE   -> 0
    UserStatus.LIMITED  -> 1
    UserStatus.EXPIRED  -> 2
    UserStatus.DISABLED -> 3
}

/** Sort users by [field]; ascending toggles direction. Online sorts nulls last in both directions. */
fun sortedUsers(users: List<User>, field: UserSortField, ascending: Boolean): List<User> {
    val base: Comparator<User> = when (field) {
        UserSortField.USERNAME -> compareBy { it.username.lowercase() }
        UserSortField.ID       -> compareBy { it.id }
        UserSortField.STATUS   -> compareBy { statusOrder(it.status) }
        UserSortField.ONLINE   -> compareBy(nullsLast()) { parseInstant(it.onlineAt) }
    }
    val directed = if (ascending) base else base.reversed()
    // Keep "never online" rows at the bottom regardless of direction.
    return if (field == UserSortField.ONLINE) {
        users.sortedWith(compareBy<User> { it.onlineAt == null }.then(directed))
    } else {
        users.sortedWith(directed)
    }
}
