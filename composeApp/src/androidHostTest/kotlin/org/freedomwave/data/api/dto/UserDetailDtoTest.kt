package org.freedomwave.data.api.dto

import org.freedomwave.data.api.remnaJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The user-detail Devices and IP-addresses sections both broke against panel 3.x because their
 * DTOs required `userUuid`, which 3.x replaced with a numeric `userId`. The mismatch threw a
 * SerializationException that surfaced to the user as "Network error, check your connection".
 *
 * Both payload shapes must decode so one build serves 2.8.x and 3.x.
 */
class UserDetailDtoTest {

    // ---- HWID devices -------------------------------------------------------------------

    @Test
    fun `hwid devices decode from a panel 3_x payload`() {
        val body = """
            {"response":{"total":1,"devices":[{
              "hwid":"hw-1","userId":42,"platform":"Android","osVersion":"15",
              "deviceModel":"Pixel","userAgent":"ua","requestIp":"1.2.3.4",
              "createdAt":"2026-01-01T00:00:00.000Z","updatedAt":"2026-01-02T00:00:00.000Z"}]}}
        """.trimIndent()

        val res = remnaJson.decodeFromString<HwidDevicesResponse>(body)
        val device = res.response.devices.single()
        assertEquals(1, res.response.total)
        assertEquals("hw-1", device.hwid)
        assertEquals(42, device.userId)
        assertEquals("1.2.3.4", device.requestIp)
        assertNull(device.userUuid)
    }

    @Test
    fun `hwid devices decode from a panel 2_8 payload`() {
        val body = """
            {"response":{"total":1,"devices":[{
              "hwid":"hw-1","userUuid":"a-uuid","platform":"Android",
              "createdAt":"2026-01-01T00:00:00.000Z","updatedAt":"2026-01-02T00:00:00.000Z"}]}}
        """.trimIndent()

        val device = remnaJson.decodeFromString<HwidDevicesResponse>(body).response.devices.single()
        assertEquals("a-uuid", device.userUuid)
        assertNull(device.userId)
        assertNull(device.requestIp)
    }

    @Test
    fun `hwid devices decode with an empty list`() {
        val body = """{"response":{"total":0,"devices":[]}}"""
        assertEquals(0, remnaJson.decodeFromString<HwidDevicesResponse>(body).response.total)
    }

    // ---- Subscription request history (the IP addresses section) ------------------------

    @Test
    fun `sub history decodes from a panel 3_x payload`() {
        val body = """
            {"response":{"total":1,"records":[{
              "id":7,"userId":42,"requestAt":"2026-01-01T00:00:00.000Z",
              "srrResponseType":"XRAY_JSON","srrRuleName":"default",
              "requestIp":"1.2.3.4","userAgent":"ua"}]}}
        """.trimIndent()

        val record = remnaJson.decodeFromString<SubHistoryResponse>(body).response.records.single()
        assertEquals(7, record.id)
        assertEquals(42, record.userId)
        assertEquals("XRAY_JSON", record.srrResponseType)
        assertEquals("default", record.srrRuleName)
        assertEquals("1.2.3.4", record.requestIp)
        assertNull(record.userUuid)
    }

    @Test
    fun `sub history decodes from a panel 2_8 payload`() {
        val body = """
            {"response":{"total":1,"records":[{
              "id":7,"userUuid":"a-uuid","requestAt":"2026-01-01T00:00:00.000Z",
              "requestIp":"1.2.3.4","userAgent":"ua"}]}}
        """.trimIndent()

        val record = remnaJson.decodeFromString<SubHistoryResponse>(body).response.records.single()
        assertEquals("a-uuid", record.userUuid)
        assertNull(record.userId)
        assertNull(record.srrResponseType)
    }

    @Test
    fun `sub history tolerates a null rule name`() {
        val body = """
            {"response":{"total":1,"records":[{
              "id":7,"userId":42,"requestAt":"2026-01-01T00:00:00.000Z",
              "srrResponseType":"BASE64","srrRuleName":null,"requestIp":null,"userAgent":null}]}}
        """.trimIndent()

        val record = remnaJson.decodeFromString<SubHistoryResponse>(body).response.records.single()
        assertNull(record.srrRuleName)
        assertNull(record.requestIp)
    }
}
