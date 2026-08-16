package org.freedomwave.data.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PanelVersionTest {

    @Test
    fun `parses plain semver`() {
        val v = PanelVersion.parse("3.2.3")
        assertEquals(3, v.major)
        assertEquals(2, v.minor)
        assertEquals(3, v.patch)
        assertEquals("3.2.3", v.raw)
    }

    @Test
    fun `treats major 3 and above as v3`() {
        assertTrue(PanelVersion.parse("3.0.0").isV3)
        assertTrue(PanelVersion.parse("3.2.3").isV3)
        assertTrue(PanelVersion.parse("4.0.0").isV3)
        assertTrue(PanelVersion.parse("10.1.0").isV3)
    }

    @Test
    fun `treats major below 3 as legacy`() {
        assertFalse(PanelVersion.parse("2.8.1").isV3)
        assertFalse(PanelVersion.parse("2.7.0").isV3)
        assertFalse(PanelVersion.parse("1.0.0").isV3)
    }

    @Test
    fun `tolerates a leading v`() {
        assertEquals(3, PanelVersion.parse("v3.2.3").major)
        assertFalse(PanelVersion.parse("V2.8.1").isV3)
    }

    @Test
    fun `tolerates prerelease and build suffixes`() {
        assertTrue(PanelVersion.parse("3.0.0-beta.1").isV3)
        assertTrue(PanelVersion.parse("3.1.0+build.42").isV3)
        assertFalse(PanelVersion.parse("2.8.1-rc1").isV3)
        assertEquals(0, PanelVersion.parse("3.0.0-beta.1").patch)
        assertEquals(1, PanelVersion.parse("3.1.0+build.42").minor)
    }

    @Test
    fun `tolerates missing minor and patch`() {
        val v = PanelVersion.parse("3")
        assertEquals(3, v.major)
        assertEquals(0, v.minor)
        assertEquals(0, v.patch)
        assertTrue(v.isV3)

        val w = PanelVersion.parse("2.8")
        assertEquals(8, w.minor)
        assertEquals(0, w.patch)
        assertFalse(w.isV3)
    }

    @Test
    fun `surrounding whitespace does not matter`() {
        assertFalse(PanelVersion.parse("  2.8.1  ").isV3)
        assertTrue(PanelVersion.parse("\t3.2.3\n").isV3)
    }

    @Test
    fun `unparseable input assumes newest contract`() {
        // Newest-wins: a version string we can't read must not silently pin the app to the
        // legacy 2.x user contract against a modern panel. Better to fail loudly on a real
        // 2.x panel than to corrupt every user route on a 3.x one.
        assertTrue(PanelVersion.parse("").isV3)
        assertTrue(PanelVersion.parse("   ").isV3)
        assertTrue(PanelVersion.parse("unknown").isV3)
        assertTrue(PanelVersion.parse("not-a-version").isV3)
        assertTrue(PanelVersion.UNKNOWN.isV3)
    }

    @Test
    fun `round trips through its stored form`() {
        listOf("2.8.1", "3.2.3", "v3.0.0-beta.1", "").forEach { raw ->
            val parsed = PanelVersion.parse(raw)
            assertEquals(parsed, PanelVersion.parse(parsed.raw), "round trip failed for '$raw'")
        }
    }
}
