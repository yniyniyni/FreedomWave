package org.freedomwave.domain.model

import org.freedomwave.data.api.dto.SquadRefDto
import org.freedomwave.data.api.dto.UserDto
import org.freedomwave.data.api.dto.UserTrafficDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Panel 3.x dropped `uuid` from the user payload and keys every user route by numeric `id`;
 * 2.8.x still sends `uuid` and keys routes by it. [User.userRef] is the single identity the
 * rest of the app passes around, so these pin down that it picks the right one from whichever
 * payload arrived.
 */
class UserRefTest {

    private fun dto(uuid: String? = null, id: Int = 7) = UserDto(
        uuid = uuid,
        id = id,
        shortUuid = "short",
        username = "alice",
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

    @Test
    fun `panel 2_8 payload refs the uuid`() {
        val user = User.from(dto(uuid = "6f1a-uuid", id = 7))
        assertEquals("6f1a-uuid", user.userRef)
        assertEquals("6f1a-uuid", user.uuid)
        assertEquals(7, user.id)
    }

    @Test
    fun `panel 3_x payload refs the numeric id`() {
        val user = User.from(dto(uuid = null, id = 42))
        assertEquals("42", user.userRef)
        assertNull(user.uuid)
        assertEquals(42, user.id)
    }

    @Test
    fun `ref is never blank so a route can always be built`() {
        assertEquals("0", User.from(dto(uuid = null, id = 0)).userRef)
        assertEquals("-1", User.from(dto(uuid = null, id = -1)).userRef)
    }

    @Test
    fun `ref shape distinguishes the two identity worlds`() {
        // UserRepository relies on this to decide whether an update body carries `id` or
        // `uuid`: a 3.x ref always parses as an Int, a 2.x uuid never does.
        assertNull(User.from(dto(uuid = "6f1a-uuid")).userRef.toIntOrNull())
        assertEquals(42, User.from(dto(uuid = null, id = 42)).userRef.toIntOrNull())
    }

    @Test
    fun `squad and node uuids are untouched by the user identity change`() {
        val user = User.from(
            dto(uuid = null, id = 1).copy(
                activeInternalSquads = listOf(SquadRefDto(uuid = "squad-uuid", name = "Squad A")),
                userTraffic = UserTrafficDto(
                    usedTrafficBytes = 0,
                    lifetimeUsedTrafficBytes = 0,
                    lastConnectedNodeUuid = "node-uuid",
                ),
            )
        )
        assertEquals(listOf("squad-uuid"), user.activeSquadUuids)
        assertEquals("node-uuid", user.lastConnectedNodeUuid)
    }
}
