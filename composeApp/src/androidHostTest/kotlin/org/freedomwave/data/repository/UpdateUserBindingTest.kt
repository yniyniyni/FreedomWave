package org.freedomwave.data.repository

import org.freedomwave.data.api.dto.UpdateUserRequest
import org.freedomwave.data.api.remnaJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * A user update body identifies its target by `id` on panel 3.x and by `uuid` on 2.8.x.
 * [bindUserRef] picks the field from the shape of the ref, which is safe because
 * `User.userRef` is `uuid ?: id.toString()` — a 3.x ref always parses as an Int, a uuid never does.
 */
class UpdateUserBindingTest {

    // The client's real configuration, so the wire assertions below reflect what actually ships.
    private val json = remnaJson

    @Test
    fun `numeric ref binds to id`() {
        val body = UpdateUserRequest(trafficLimitBytes = 100).bindUserRef("42")
        assertEquals(42, body.id)
        assertNull(body.uuid)
    }

    @Test
    fun `uuid ref binds to uuid`() {
        val body = UpdateUserRequest(trafficLimitBytes = 100).bindUserRef("6f1a-2b3c-uuid")
        assertEquals("6f1a-2b3c-uuid", body.uuid)
        assertNull(body.id)
    }

    @Test
    fun `binding never sends both identifiers`() {
        // The panel rejects an ambiguous body; exactly one identifier must be present.
        listOf("42", "6f1a-uuid").forEach { ref ->
            val body = UpdateUserRequest().bindUserRef(ref)
            assertTrue(
                (body.id == null) != (body.uuid == null),
                "expected exactly one identifier for ref '$ref', got id=${body.id} uuid=${body.uuid}"
            )
        }
    }

    @Test
    fun `binding overwrites any identifier already set`() {
        val stale = UpdateUserRequest(uuid = "stale-uuid", id = 999)
        val v3 = stale.bindUserRef("42")
        assertEquals(42, v3.id)
        assertNull(v3.uuid)

        val v2 = stale.bindUserRef("fresh-uuid")
        assertEquals("fresh-uuid", v2.uuid)
        assertNull(v2.id)
    }

    @Test
    fun `binding preserves the edited fields`() {
        val body = UpdateUserRequest(
            trafficLimitBytes = 5000,
            hwidDeviceLimit = 3,
            description = "note",
        ).bindUserRef("42")

        assertEquals(5000, body.trafficLimitBytes)
        assertEquals(3, body.hwidDeviceLimit)
        assertEquals("note", body.description)
    }

    @Test
    fun `serialized v3 body carries id and omits uuid`() {
        val wire = json.encodeToString(UpdateUserRequest(trafficLimitBytes = 1).bindUserRef("42"))
        assertTrue(wire.contains("\"id\":42"), wire)
        assertFalse(wire.contains("uuid"), wire)
    }

    @Test
    fun `serialized v2 body carries uuid and omits id`() {
        val wire = json.encodeToString(UpdateUserRequest(trafficLimitBytes = 1).bindUserRef("ab-cd"))
        assertTrue(wire.contains("\"uuid\":\"ab-cd\""), wire)
        assertFalse(wire.contains("\"id\""), wire)
    }
}
