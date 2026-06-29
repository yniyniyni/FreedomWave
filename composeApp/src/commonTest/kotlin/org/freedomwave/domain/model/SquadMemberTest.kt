package org.freedomwave.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SquadMemberTest {

    private fun user(
        name: String,
        internalSquadUuids: List<String> = emptyList(),
        externalSquadUuid: String? = null,
        status: UserStatus = UserStatus.ACTIVE,
    ) = User(
        uuid = "u-$name", id = 0, shortUuid = "s-$name", username = name, status = status,
        trafficLimitBytes = 0, trafficLimitStrategy = "NO_RESET", expireAt = "",
        usedTrafficBytes = 0, lifetimeUsedTrafficBytes = 0, subscriptionUrl = "",
        onlineAt = null, email = null, tag = null, description = null, telegramId = null,
        hwidDeviceLimit = null, activeSquads = emptyList(), activeSquadUuids = internalSquadUuids,
        externalSquadUuid = externalSquadUuid, lastConnectedNodeUuid = null, createdAt = "",
    )

    @Test
    fun internalMembers_filters_by_membership_and_sorts_case_insensitively() {
        val users = listOf(
            user("Bob", internalSquadUuids = listOf("sq1")),
            user("alice", internalSquadUuids = listOf("sq1", "sq2")),
            user("carol", internalSquadUuids = listOf("sq2")),
        )
        val members = SquadMember.internalMembers(users, "sq1")
        assertEquals(listOf("alice", "Bob"), members.map { it.username })
    }

    @Test
    fun externalMembers_filters_by_single_external_squad() {
        val users = listOf(
            user("dave", externalSquadUuid = "ext1"),
            user("erin", externalSquadUuid = "ext2"),
            user("frank", externalSquadUuid = null),
        )
        val members = SquadMember.externalMembers(users, "ext1")
        assertEquals(listOf("dave"), members.map { it.username })
    }
}
