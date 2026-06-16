package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.dto.CreateNodeRequest
import art.yniyniyni.freedomwave.data.api.dto.NodeConfigProfileBody
import art.yniyniyni.freedomwave.data.api.dto.UpdateNodeRequest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NodeRequestSerializationTest {

    private val json = Json { encodeDefaults = false; explicitNulls = true }
    private val profile = NodeConfigProfileBody("cp-uuid", listOf("in-1"))

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
