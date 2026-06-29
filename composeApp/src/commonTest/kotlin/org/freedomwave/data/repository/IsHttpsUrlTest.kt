package org.freedomwave.data.repository

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsHttpsUrlTest {

    @Test
    fun `accepts https urls with a host`() {
        assertTrue(isHttpsUrl("https://panel.example.com"))
        assertTrue(isHttpsUrl("https://panel.example.com:8443/api"))
        assertTrue(isHttpsUrl("  https://panel.example.com/  "))
        assertTrue(isHttpsUrl("HTTPS://panel.example.com"))
    }

    @Test
    fun `rejects non-https schemes`() {
        assertFalse(isHttpsUrl("http://panel.example.com"))
        assertFalse(isHttpsUrl("ftp://panel.example.com"))
        assertFalse(isHttpsUrl("ws://panel.example.com"))
    }

    @Test
    fun `rejects missing scheme or host`() {
        assertFalse(isHttpsUrl("panel.example.com"))
        assertFalse(isHttpsUrl("https://"))
        assertFalse(isHttpsUrl("https:///path"))
        assertFalse(isHttpsUrl(""))
        assertFalse(isHttpsUrl("   "))
    }
}
