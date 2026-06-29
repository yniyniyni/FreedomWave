package org.freedomwave.data.repository

import org.freedomwave.ui.feature.nodes.NodeFormInput
import org.freedomwave.ui.feature.nodes.buildCreateRequest
import org.freedomwave.ui.feature.nodes.buildNodeCompose
import org.freedomwave.ui.feature.nodes.gbToBytes
import org.freedomwave.ui.feature.nodes.parseTags
import org.freedomwave.ui.feature.nodes.portOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NodeFormLogicTest {

    @Test fun `parseTags uppercases trims and drops invalid`() {
        assertEquals(listOf("TAG_ONE", "TAG2"), parseTags(" tag_one , tag2 "))
        assertEquals(emptyList(), parseTags(""))
        assertEquals(listOf("A_B:C"), parseTags("a_b:c, bad tag!"))
    }

    @Test fun `gbToBytes converts and guards`() {
        assertEquals(1_073_741_824L, gbToBytes("1"))
        assertNull(gbToBytes(""))
        assertNull(gbToBytes("abc"))
    }

    @Test fun `portOrNull parses range`() {
        assertEquals(2222, portOrNull("2222"))
        assertNull(portOrNull(""))
        assertNull(portOrNull("70000"))
        assertNull(portOrNull("0"))
    }

    @Test fun `buildCreateRequest maps fields and omits empties`() {
        val req = buildCreateRequest(
            NodeFormInput(
                name = "n", address = "a", port = 2222, countryCode = "IT",
                trackingActive = true, multiplier = 1.5, trafficLimitBytes = 1024L,
                resetDay = 5, notifyPercent = 90, tags = listOf("X"),
                profileUuid = "cp", inbounds = listOf("in1"),
            )
        )
        assertEquals("n", req.name)
        assertEquals(2222, req.port)
        assertEquals("IT", req.countryCode)
        assertTrue(req.isTrafficTrackingActive)
        assertEquals(1024L, req.trafficLimitBytes)
        assertEquals("cp", req.configProfile.activeConfigProfileUuid)
        assertEquals(listOf("in1"), req.configProfile.activeInbounds)
    }

    @Test fun `buildCreateRequest drops traffic fields when tracking off`() {
        val req = buildCreateRequest(
            NodeFormInput(
                name = "n", address = "a", port = null, countryCode = "XX",
                trackingActive = false, multiplier = null, trafficLimitBytes = 999L,
                resetDay = 5, notifyPercent = 90, tags = emptyList(),
                profileUuid = "cp", inbounds = emptyList(),
            )
        )
        assertNull(req.trafficLimitBytes)
        assertNull(req.trafficResetDay)
        assertNull(req.notifyPercent)
        assertNull(req.tags)
    }

    @Test fun `buildNodeCompose embeds key and port`() {
        val yml = buildNodeCompose("PUBKEY123", 2222)
        assertTrue(yml.contains("SSL_CERT=PUBKEY123"))
        assertTrue(yml.contains("2222"))
        assertTrue(yml.contains("remnawave/node"))
    }
}
