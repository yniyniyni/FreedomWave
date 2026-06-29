package org.freedomwave.data.repository

import org.freedomwave.data.api.dto.ReorderNodesRequest
import org.freedomwave.data.api.dto.reorderNodesPayload
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeReorderSerializationTest {

    private val json = Json { encodeDefaults = false; explicitNulls = true }

    @Test fun `payload maps uuids to 0-based viewPosition in order`() {
        val payload = reorderNodesPayload(listOf("a", "b", "c"))
        assertEquals(listOf(0, 1, 2), payload.map { it.viewPosition })
        assertEquals(listOf("a", "b", "c"), payload.map { it.uuid })
    }

    @Test fun `request serializes to expected json shape`() {
        val out = json.encodeToString(ReorderNodesRequest(reorderNodesPayload(listOf("a", "b"))))
        assertEquals(
            """{"nodes":[{"uuid":"a","viewPosition":0},{"uuid":"b","viewPosition":1}]}""",
            out,
        )
    }

    @Test fun `empty list serializes to empty nodes array`() {
        val out = json.encodeToString(ReorderNodesRequest(reorderNodesPayload(emptyList())))
        assertTrue(out.contains("\"nodes\":[]"))
    }
}
