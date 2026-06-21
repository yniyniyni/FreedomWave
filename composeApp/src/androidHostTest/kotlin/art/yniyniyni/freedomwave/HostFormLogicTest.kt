package art.yniyniyni.freedomwave

import art.yniyniyni.freedomwave.ui.feature.hosts.HostFormInput
import art.yniyniyni.freedomwave.ui.feature.hosts.buildCreateRequest
import art.yniyniyni.freedomwave.ui.feature.hosts.buildUpdateRequest
import art.yniyniyni.freedomwave.ui.feature.hosts.parseHostTag
import art.yniyniyni.freedomwave.ui.feature.hosts.portOrNull
import art.yniyniyni.freedomwave.ui.feature.hosts.remarkValid
import art.yniyniyni.freedomwave.ui.feature.hosts.serverDescriptionOrNull
import art.yniyniyni.freedomwave.ui.feature.hosts.vlessRouteIdOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HostFormLogicTest {

    // remarkValid

    @Test fun `remarkValid rejects empty string`() {
        assertFalse(remarkValid(""))
    }

    @Test fun `remarkValid accepts single char`() {
        assertTrue(remarkValid("x"))
    }

    @Test fun `remarkValid accepts 40-char string`() {
        assertTrue(remarkValid("a".repeat(40)))
    }

    @Test fun `remarkValid rejects 41-char string`() {
        assertFalse(remarkValid("a".repeat(41)))
    }

    // portOrNull

    @Test fun `portOrNull parses valid port`() {
        assertEquals(443, portOrNull("443"))
    }

    @Test fun `portOrNull rejects zero`() {
        assertNull(portOrNull("0"))
    }

    @Test fun `portOrNull rejects port above 65535`() {
        assertNull(portOrNull("70000"))
    }

    @Test fun `portOrNull rejects empty string`() {
        assertNull(portOrNull(""))
    }

    @Test fun `portOrNull rejects non-numeric`() {
        assertNull(portOrNull("abc"))
    }

    // vlessRouteIdOrNull

    @Test fun `vlessRouteIdOrNull returns null for blank`() {
        assertNull(vlessRouteIdOrNull(""))
    }

    @Test fun `vlessRouteIdOrNull accepts zero`() {
        assertEquals(0, vlessRouteIdOrNull("0"))
    }

    @Test fun `vlessRouteIdOrNull accepts max value 65535`() {
        assertEquals(65535, vlessRouteIdOrNull("65535"))
    }

    @Test fun `vlessRouteIdOrNull rejects value above 65535`() {
        assertNull(vlessRouteIdOrNull("70000"))
    }

    // parseHostTag

    @Test fun `parseHostTag uppercases valid tag`() {
        assertEquals("MY:TAG_1", parseHostTag("my:tag_1"))
    }

    @Test fun `parseHostTag rejects tag with spaces and special chars`() {
        assertNull(parseHostTag("bad tag!"))
    }

    @Test fun `parseHostTag rejects empty string`() {
        assertNull(parseHostTag(""))
    }

    // serverDescriptionOrNull

    @Test fun `serverDescriptionOrNull returns null for blank`() {
        assertNull(serverDescriptionOrNull("  "))
    }

    @Test fun `serverDescriptionOrNull returns trimmed value`() {
        assertEquals("hello", serverDescriptionOrNull("hello"))
    }

    @Test fun `serverDescriptionOrNull truncates to 30 chars`() {
        assertEquals(30, serverDescriptionOrNull("a".repeat(40))?.length)
    }

    // buildCreateRequest

    private fun sampleInput(
        alpn: String? = "h2",
        tag: String? = "OK",
        port: Int = 443,
        isHidden: Boolean = false,
        shuffleHost: Boolean = false,
    ) = HostFormInput(
        configProfileUuid = "cp-uuid",
        configProfileInboundUuid = "inbound-uuid",
        remark = "My Host",
        address = "example.com",
        port = port,
        path = null,
        sni = null,
        host = null,
        alpn = alpn,
        fingerprint = null,
        securityLayer = "TLS",
        serverDescription = null,
        tag = tag,
        isDisabled = false,
        isHidden = isHidden,
        overrideSniFromAddress = false,
        keepSniBlank = false,
        allowInsecure = false,
        vlessRouteId = null,
        shuffleHost = shuffleHost,
        mihomoX25519 = false,
        nodes = emptyList(),
        xrayJsonTemplateUuid = null,
    )

    @Test fun `buildCreateRequest maps remark address port alpn inbound and tag`() {
        val req = buildCreateRequest(sampleInput())
        assertEquals("My Host", req.remark)
        assertEquals("example.com", req.address)
        assertEquals(443, req.port)
        assertEquals("h2", req.alpn)
        assertEquals("cp-uuid", req.inbound.configProfileUuid)
        assertEquals("OK", req.tag)
    }

    // buildUpdateRequest

    @Test fun `buildUpdateRequest sets uuid and reflects isHidden and shuffleHost`() {
        val req = buildUpdateRequest("host-uuid-123", sampleInput(isHidden = true, shuffleHost = true))
        assertEquals("host-uuid-123", req.uuid)
        assertTrue(req.isHidden)
        assertTrue(req.shuffleHost)
    }

    @Test fun `buildUpdateRequest reflects false booleans`() {
        val req = buildUpdateRequest("host-uuid-456", sampleInput(isHidden = false, shuffleHost = false))
        assertFalse(req.isHidden)
        assertFalse(req.shuffleHost)
    }
}
