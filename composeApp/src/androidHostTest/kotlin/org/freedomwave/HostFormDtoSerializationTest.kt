package org.freedomwave

import org.freedomwave.data.api.dto.CreateHostRequest
import org.freedomwave.data.api.dto.HostDto
import org.freedomwave.data.api.dto.HostInboundBody
import org.freedomwave.data.api.dto.UpdateHostRequest
import org.freedomwave.domain.model.Host
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class HostFormDtoSerializationTest {

    private val json = Json { encodeDefaults = false; explicitNulls = true }
    private val decodeJson = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    @Test
    fun `create request omits unset optional fields`() {
        val out = json.encodeToString(
            CreateHostRequest(
                inbound = HostInboundBody("p", "i"),
                remark  = "r",
                address = "a",
                port    = 443,
                sni     = "x",
            )
        )
        assertTrue(out.contains("\"remark\""))
        assertTrue(out.contains("\"inbound\""))
        assertTrue(out.contains("\"port\""))
        assertTrue(out.contains("\"sni\""))
        assertFalse(out.contains("\"path\""), "unset path must be omitted")
        assertFalse(out.contains("\"vlessRouteId\""), "unset vlessRouteId must be omitted")
        assertFalse(out.contains("\"xrayJsonTemplateUuid\""), "unset xrayJsonTemplateUuid must be omitted")
        assertFalse(out.contains("\"tag\""), "unset tag must be omitted")
    }

    @Test
    fun `update request sends false toggles`() {
        val out = json.encodeToString(
            UpdateHostRequest(
                uuid    = "u",
                inbound = HostInboundBody("p", "i"),
                remark  = "r",
                address = "a",
                port    = 443,
            )
        )
        assertTrue(out.contains("\"isHidden\":false"), "isHidden=false must be sent")
        assertTrue(out.contains("\"allowInsecure\":false"), "allowInsecure=false must be sent")
        assertTrue(out.contains("\"shuffleHost\":false"), "shuffleHost=false must be sent")
        assertTrue(out.contains("\"securityLayer\":\"DEFAULT\""), "securityLayer must be sent")
        assertTrue(out.contains("\"nodes\":[]"), "nodes=[] must be sent")
        assertFalse(out.contains("\"path\""), "unset path must be omitted")
        assertFalse(out.contains("\"tag\""), "unset tag must be omitted")
    }

    @Test
    fun `host dto decodes nested inbound and nulls`() {
        val body = """{"uuid":"h","remark":"r","address":"a","port":443,"inbound":{"configProfileUuid":"cp","configProfileInboundUuid":"ci"},"path":null,"vlessRouteId":null,"nodes":[]}"""
        val host = Host.from(decodeJson.decodeFromString<HostDto>(body))
        assertEquals("ci", host.configProfileInboundUuid)
        assertEquals("cp", host.configProfileUuid)
        assertNull(host.vlessRouteId)
    }
}
