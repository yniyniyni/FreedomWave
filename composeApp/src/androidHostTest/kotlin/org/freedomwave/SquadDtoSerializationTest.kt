package org.freedomwave

import org.freedomwave.data.api.dto.CreateInternalSquadRequest
import org.freedomwave.data.api.dto.CustomRemarksDto
import org.freedomwave.data.api.dto.ExternalSquadDetailDto
import org.freedomwave.data.api.dto.HwidSettingsDto
import org.freedomwave.data.api.dto.SquadHostOverridesDto
import org.freedomwave.data.api.dto.SquadTemplateRefDto
import org.freedomwave.data.api.dto.SubscriptionSettingsOverrideDto
import org.freedomwave.data.api.dto.UpdateExternalSquadRequest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SquadDtoSerializationTest {

    private val json = Json { encodeDefaults = false; explicitNulls = true }
    private val decodeJson = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    @Test
    fun `subscriptionSettings omits null overrides`() {
        val out = json.encodeToString(SubscriptionSettingsOverrideDto(profileTitle = "t"))
        assertTrue(out.contains("\"profileTitle\""), "profileTitle must be present")
        assertFalse(out.contains("\"supportLink\""), "supportLink must be omitted when null")
        assertFalse(out.contains("\"randomizeHosts\""), "randomizeHosts must be omitted when null")
    }

    @Test
    fun `internal create request always includes inbounds`() {
        // Backend requires `inbounds` to be present; with encodeDefaults=false a plain
        // default would be dropped, producing a 400 "Validation failed".
        val out = json.encodeToString(CreateInternalSquadRequest(name = "testsquad", inbounds = emptyList()))
        assertTrue(out.contains("\"inbounds\":[]"), "inbounds must be present even when empty")
    }

    @Test
    fun `external request omits null sections`() {
        val out = json.encodeToString(
            UpdateExternalSquadRequest(
                uuid                 = "u",
                name                 = "n",
                templates            = emptyList(),
                subscriptionSettings = SubscriptionSettingsOverrideDto(),
                hostOverrides        = SquadHostOverridesDto(),
            )
        )
        assertTrue(out.contains("\"templates\":[]"), "templates must be present as empty array")
        assertFalse(out.contains("\"hwidSettings\""), "hwidSettings must be omitted when null")
        assertFalse(out.contains("\"customRemarks\""), "customRemarks must be omitted when null")
        assertFalse(out.contains("\"subpageConfigUuid\""), "subpageConfigUuid must be omitted when null")
        assertFalse(out.contains("\"responseHeaders\""), "responseHeaders must be omitted when null")
    }

    @Test
    fun `external detail decodes nested`() {
        val raw = """
            {
              "uuid": "u",
              "name": "n",
              "info": {"membersCount": 0},
              "templates": [{"templateUuid": "t", "templateType": "XRAY_JSON"}],
              "subscriptionSettings": {"profileTitle": "p"},
              "hwidSettings": {"enabled": true, "fallbackDeviceLimit": 3, "maxDevicesAnnounce": null},
              "customRemarks": {
                "expiredUsers": ["x"],
                "limitedUsers": ["y"],
                "disabledUsers": ["z"],
                "emptyHosts": ["a"],
                "HWIDMaxDevicesExceeded": ["b"],
                "HWIDNotSupported": ["c"]
              },
              "subpageConfigUuid": null,
              "createdAt": "2026-01-01"
            }
        """.trimIndent()
        val dto = decodeJson.decodeFromString<ExternalSquadDetailDto>(raw)
        assertEquals("XRAY_JSON", dto.templates[0].templateType)
        assertEquals("p", dto.subscriptionSettings?.profileTitle)
        assertEquals(3, dto.hwidSettings?.fallbackDeviceLimit)
        assertEquals(listOf("b"), dto.customRemarks?.hwidMaxDevicesExceeded)
        assertNull(dto.subpageConfigUuid)
    }
}
