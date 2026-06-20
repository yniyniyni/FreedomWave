package art.yniyniyni.freedomwave

import art.yniyniyni.freedomwave.data.api.dto.SquadTemplateRefDto
import art.yniyniyni.freedomwave.ui.feature.squads.ExternalSquadFormInput
import art.yniyniyni.freedomwave.ui.feature.squads.announceValid
import art.yniyniyni.freedomwave.ui.feature.squads.buildExternalSquadRequest
import art.yniyniyni.freedomwave.ui.feature.squads.nameValid
import art.yniyniyni.freedomwave.ui.feature.squads.remarksValid
import art.yniyniyni.freedomwave.ui.feature.squads.squadVlessRouteIdOrNull
import art.yniyniyni.freedomwave.ui.feature.squads.updateIntervalOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExternalSquadFormLogicTest {

    private fun baseline() = ExternalSquadFormInput(
        name = "Squad",
        templates = emptyMap(),
        profileTitle = null,
        supportLink = null,
        profileUpdateInterval = null,
        isProfileWebpageUrlEnabled = null,
        serveJsonAtBaseSubscription = null,
        isShowCustomRemarks = null,
        happAnnounce = null,
        happRouting = null,
        randomizeHosts = null,
        serverDescription = null,
        vlessRouteId = null,
        headers = emptyList(),
        hwidOverride = false,
        hwidEnabled = false,
        hwidFallbackDeviceLimit = 0,
        hwidMaxDevicesAnnounce = null,
        remarksOverride = false,
        expiredUsers = emptyList(),
        limitedUsers = emptyList(),
        disabledUsers = emptyList(),
        emptyHosts = emptyList(),
        hwidMaxDevicesExceeded = emptyList(),
        hwidNotSupported = emptyList(),
        subpageConfigUuid = null,
    )

    // ── nameValid ────────────────────────────────────────────────────────────

    @Test fun `nameValid rejects empty string`() {
        assertFalse(nameValid(""), "empty should be invalid")
    }

    @Test fun `nameValid accepts two-char string`() {
        assertTrue(nameValid("ab"))
    }

    @Test fun `nameValid accepts 30-char string`() {
        assertTrue(nameValid("a".repeat(30)))
    }

    @Test fun `nameValid rejects 31-char string`() {
        assertFalse(nameValid("a".repeat(31)), "31 chars should be invalid")
    }

    // ── updateIntervalOrNull ─────────────────────────────────────────────────

    @Test fun `updateIntervalOrNull rejects zero`() {
        assertNull(updateIntervalOrNull("0"))
    }

    @Test fun `updateIntervalOrNull accepts one`() {
        assertEquals(1, updateIntervalOrNull("1"))
    }

    @Test fun `updateIntervalOrNull rejects empty string`() {
        assertNull(updateIntervalOrNull(""))
    }

    @Test fun `updateIntervalOrNull rejects non-numeric`() {
        assertNull(updateIntervalOrNull("x"))
    }

    // ── announceValid ────────────────────────────────────────────────────────

    @Test fun `announceValid accepts 200-char string`() {
        assertTrue(announceValid("a".repeat(200)))
    }

    @Test fun `announceValid rejects 201-char string`() {
        assertFalse(announceValid("a".repeat(201)), "201 chars should be invalid")
    }

    // ── squadVlessRouteIdOrNull ──────────────────────────────────────────────

    @Test fun `squadVlessRouteIdOrNull returns null for blank`() {
        assertNull(squadVlessRouteIdOrNull(""))
    }

    @Test fun `squadVlessRouteIdOrNull accepts zero`() {
        assertEquals(0, squadVlessRouteIdOrNull("0"))
    }

    @Test fun `squadVlessRouteIdOrNull accepts max value 65535`() {
        assertEquals(65535, squadVlessRouteIdOrNull("65535"))
    }

    @Test fun `squadVlessRouteIdOrNull rejects value above 65535`() {
        assertNull(squadVlessRouteIdOrNull("70000"))
    }

    // ── remarksValid ─────────────────────────────────────────────────────────

    @Test fun `remarksValid returns true when override is off`() {
        assertTrue(remarksValid(baseline().copy(remarksOverride = false)))
    }

    @Test fun `remarksValid returns true when override on and all six lists non-empty`() {
        val input = baseline().copy(
            remarksOverride = true,
            expiredUsers = listOf("expired msg"),
            limitedUsers = listOf("limited msg"),
            disabledUsers = listOf("disabled msg"),
            emptyHosts = listOf("empty hosts msg"),
            hwidMaxDevicesExceeded = listOf("hwid exceeded msg"),
            hwidNotSupported = listOf("hwid not supported msg"),
        )
        assertTrue(remarksValid(input))
    }

    @Test fun `remarksValid returns false when override on and one list is empty`() {
        val input = baseline().copy(
            remarksOverride = true,
            expiredUsers = listOf("expired msg"),
            limitedUsers = listOf("limited msg"),
            disabledUsers = listOf("disabled msg"),
            emptyHosts = emptyList(), // missing
            hwidMaxDevicesExceeded = listOf("hwid exceeded msg"),
            hwidNotSupported = listOf("hwid not supported msg"),
        )
        assertFalse(remarksValid(input), "missing emptyHosts should make remarks invalid")
    }

    // ── buildExternalSquadRequest ────────────────────────────────────────────

    @Test fun `buildExternalSquadRequest maps profileTitle and leaves supportLink null`() {
        val input = baseline().copy(profileTitle = "t")
        val req = buildExternalSquadRequest("squad-uuid", input)
        assertEquals("t", req.subscriptionSettings.profileTitle)
        assertNull(req.subscriptionSettings.supportLink)
    }

    @Test fun `buildExternalSquadRequest strips blank header key from responseHeaders`() {
        val input = baseline().copy(
            headers = listOf("X" to "1", "" to "2"),
        )
        val req = buildExternalSquadRequest("squad-uuid", input)
        assertEquals(mapOf<String, String>("X" to "1"), req.responseHeaders)
    }

    @Test fun `buildExternalSquadRequest sets hwidSettings to null when hwidOverride is false`() {
        val input = baseline().copy(hwidOverride = false)
        val req = buildExternalSquadRequest("squad-uuid", input)
        assertNull(req.hwidSettings)
    }

    @Test fun `buildExternalSquadRequest populates customRemarks when remarksOverride is true`() {
        val input = baseline().copy(
            remarksOverride = true,
            expiredUsers = listOf("e"),
            limitedUsers = listOf("l"),
            disabledUsers = listOf("d"),
            emptyHosts = listOf("h"),
            hwidMaxDevicesExceeded = listOf("m"),
            hwidNotSupported = listOf("n"),
        )
        val req = buildExternalSquadRequest("squad-uuid", input)
        assertNotNull(req.customRemarks)
    }

    @Test fun `buildExternalSquadRequest maps templates list correctly`() {
        val input = baseline().copy(
            templates = mapOf("XRAY_JSON" to "u"),
        )
        val req = buildExternalSquadRequest("squad-uuid", input)
        assertEquals(1, req.templates.size)
        assertEquals(SquadTemplateRefDto(templateUuid = "u", templateType = "XRAY_JSON"), req.templates[0])
    }
}
