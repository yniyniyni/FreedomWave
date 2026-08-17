package org.freedomwave

import org.freedomwave.ui.feature.hosts.MAX_HOST_TAGS
import org.freedomwave.ui.feature.hosts.formatHostTags
import org.freedomwave.ui.feature.hosts.parseHostTags
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Panel 2.8.1 replaced the single host `tag` with a `tags` array: up to 10 entries, each
 * matching `^[A-Z0-9_:]+$` and at most 36 characters. The form takes them as one
 * comma-separated field, so these pin the parse/format round trip and the panel's limits.
 */
class HostTagsTest {

    @Test
    fun `splits a comma separated list`() {
        assertEquals(listOf("EU", "FAST"), parseHostTags("EU,FAST"))
        assertEquals(listOf("EU", "FAST"), parseHostTags("EU, FAST"))
        assertEquals(listOf("EU", "FAST"), parseHostTags("  EU ,  FAST  "))
    }

    @Test
    fun `uppercases like the panel expects`() {
        assertEquals(listOf("EU", "FAST"), parseHostTags("eu, fast"))
    }

    @Test
    fun `keeps the characters the panel allows`() {
        // Uppercase, digits, underscore and colon are all valid per the backend regex.
        assertEquals(listOf("EU_WEST", "TIER:1", "V2"), parseHostTags("eu_west, tier:1, v2"))
    }

    @Test
    fun `drops entries with characters the panel rejects`() {
        assertEquals(listOf("EU"), parseHostTags("EU, bad-tag, also bad, ok!"))
        assertEquals(emptyList(), parseHostTags("-, !, @@"))
    }

    @Test
    fun `drops blank entries rather than emitting empty tags`() {
        assertEquals(listOf("EU"), parseHostTags("EU,,, ,"))
        assertEquals(emptyList(), parseHostTags(""))
        assertEquals(emptyList(), parseHostTags("   "))
        assertEquals(emptyList(), parseHostTags(",,,"))
    }

    @Test
    fun `drops tags longer than the panel allows`() {
        val ok = "A".repeat(36)
        val tooLong = "A".repeat(37)
        assertEquals(listOf(ok), parseHostTags("$ok,$tooLong"))
    }

    @Test
    fun `deduplicates repeated tags`() {
        assertEquals(listOf("EU"), parseHostTags("EU, eu, EU"))
    }

    @Test
    fun `caps at the panel maximum`() {
        val many = (1..15).joinToString(",") { "TAG$it" }
        val parsed = parseHostTags(many)
        assertEquals(MAX_HOST_TAGS, parsed.size)
        assertEquals("TAG1", parsed.first())
        assertTrue(parsed.none { it == "TAG11" })
    }

    @Test
    fun `format round trips back through parse`() {
        val tags = listOf("EU", "TIER:1", "FAST_2")
        assertEquals(tags, parseHostTags(formatHostTags(tags)))
    }

    @Test
    fun `format of no tags is empty so the field shows blank`() {
        assertEquals("", formatHostTags(emptyList()))
        assertEquals(emptyList(), parseHostTags(formatHostTags(emptyList())))
    }
}
