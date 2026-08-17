package org.freedomwave.data.api.dto

import org.freedomwave.data.api.remnaJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * `GET /api/keygen` returns the same value on both panel versions — the encoded node payload
 * that goes into the node's `SECRET_KEY` — but 3.0.0 renamed the JSON field from `pubKey` to
 * `secretKey`. The route and the value are otherwise identical, so the DTO accepts either.
 */
class KeygenDtoTest {

    @Test
    fun `reads the panel 2_8 field name`() {
        val body = """{"response":{"pubKey":"payload-abc"}}"""
        val res = remnaJson.decodeFromString<KeygenResponse>(body)
        assertEquals("payload-abc", res.response.nodeSecretKey)
    }

    @Test
    fun `reads the panel 3_x field name`() {
        val body = """{"response":{"secretKey":"payload-xyz"}}"""
        val res = remnaJson.decodeFromString<KeygenResponse>(body)
        assertEquals("payload-xyz", res.response.nodeSecretKey)
    }

    @Test
    fun `prefers secretKey when a panel somehow sends both`() {
        val body = """{"response":{"pubKey":"old","secretKey":"new"}}"""
        val res = remnaJson.decodeFromString<KeygenResponse>(body)
        assertEquals("new", res.response.nodeSecretKey)
    }

    @Test
    fun `neither field present yields null rather than throwing`() {
        val body = """{"response":{}}"""
        val res = remnaJson.decodeFromString<KeygenResponse>(body)
        assertNull(res.response.nodeSecretKey)
    }
}
