package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.dto.ReorderSquadsRequest
import art.yniyniyni.freedomwave.data.api.dto.reorderSquadsPayload
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SquadReorderSerializationTest {

    private val json = Json { encodeDefaults = false; explicitNulls = true }

    @Test fun `payload maps uuids to 0-based viewPosition in order`() {
        val payload = reorderSquadsPayload(listOf("a", "b", "c"))
        assertEquals(listOf(0, 1, 2), payload.map { it.viewPosition })
        assertEquals(listOf("a", "b", "c"), payload.map { it.uuid })
    }

    @Test fun `request serializes to expected json shape with items key`() {
        val out = json.encodeToString(ReorderSquadsRequest(reorderSquadsPayload(listOf("a", "b"))))
        assertEquals(
            """{"items":[{"uuid":"a","viewPosition":0},{"uuid":"b","viewPosition":1}]}""",
            out,
        )
    }

    @Test fun `empty list serializes to empty items array`() {
        val out = json.encodeToString(ReorderSquadsRequest(reorderSquadsPayload(emptyList())))
        assertTrue(out.contains("\"items\":[]"))
    }
}
