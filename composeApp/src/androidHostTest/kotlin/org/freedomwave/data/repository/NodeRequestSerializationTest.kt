package org.freedomwave.data.repository

import org.freedomwave.data.api.dto.CreateNodeRequest
import org.freedomwave.data.api.dto.NodeConfigProfileBody
import org.freedomwave.data.api.dto.NodeDto
import org.freedomwave.data.api.dto.UpdateNodeRequest
import org.freedomwave.domain.model.Node
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NodeRequestSerializationTest {

    private val json = Json { encodeDefaults = false; explicitNulls = true }
    // Mirrors the app's authed client decode settings.
    private val decodeJson = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
    private val profile = NodeConfigProfileBody("cp-uuid", listOf("in-1"))

    @Test
    fun `node response with activeInbounds as objects decodes`() {
        // Regression: the GET /api/nodes response returns configProfile.activeInbounds
        // as an array of inbound OBJECTS, not UUID strings. Decoding must not throw,
        // and Node.from must extract the inbound UUIDs.
        val body = """
            {"uuid":"u1","name":"n","address":"1.2.3.4","isConnected":true,"isDisabled":false,
             "isConnecting":false,"isTrafficTrackingActive":false,"viewPosition":1,"countryCode":"IT",
             "createdAt":"2024-01-01T00:00:00.000Z","updatedAt":"2024-01-01T00:00:00.000Z",
             "configProfile":{"activeConfigProfileUuid":"cp-1","activeInbounds":[
                {"uuid":"in-1","profileUuid":"cp-1","tag":"VLESS","type":"vless","network":"tcp","security":"reality","port":443}
             ]}}
        """.trimIndent()
        val node = Node.from(decodeJson.decodeFromString<NodeDto>(body))
        assertEquals("cp-1", node.activeConfigProfileUuid)
        assertEquals(listOf("in-1"), node.activeInbounds)
    }

    @Test
    fun `create omits unset optional fields`() {
        val out = json.encodeToString(
            CreateNodeRequest(name = "n1", address = "1.2.3.4", configProfile = profile)
        )
        assertTrue(out.contains("\"name\":\"n1\""))
        assertTrue(out.contains("\"address\":\"1.2.3.4\""))
        assertTrue(out.contains("\"configProfile\""))
        assertFalse(out.contains("\"port\""), "unset port must be omitted")
        assertFalse(out.contains("\"tags\""), "unset tags must be omitted")
        assertFalse(out.contains("\"countryCode\""), "default countryCode must be omitted")
        assertFalse(out.contains("\"consumptionMultiplier\""), "null multiplier must be omitted")
    }

    @Test
    fun `update always sends managed fields including tracking-off`() {
        val out = json.encodeToString(
            UpdateNodeRequest(
                uuid = "u1", name = "n1", address = "1.2.3.4",
                isTrafficTrackingActive = false, configProfile = profile,
            )
        )
        assertTrue(out.contains("\"uuid\":\"u1\""))
        assertTrue(out.contains("\"name\":\"n1\""))
        assertTrue(out.contains("\"isTrafficTrackingActive\":false"), "tracking=false must still be sent")
        assertTrue(out.contains("\"consumptionMultiplier\":1.0"))
        assertTrue(out.contains("\"tags\":[]"))
        assertFalse(out.contains("\"trafficLimitBytes\""), "unset trafficLimitBytes must be omitted (not null)")
        assertFalse(out.contains("\"port\""), "unset port must be omitted (not null)")
    }
}
