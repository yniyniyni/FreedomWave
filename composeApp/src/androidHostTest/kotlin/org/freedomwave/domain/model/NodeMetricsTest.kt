package org.freedomwave.domain.model

import kotlinx.serialization.json.Json
import org.freedomwave.data.api.dto.NodesMetricsResponse
import org.freedomwave.util.parseHumanBytes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * `/api/system/nodes/metrics` returns tag traffic pre-formatted ("45.62 GiB"), so the client
 * parses it back to bytes to render with its own localized units.
 */
class NodeMetricsTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesPanelFormattedSizes() {
        assertEquals(0L, parseHumanBytes("0"))
        assertEquals(512L, parseHumanBytes("512 B"))
        assertEquals(1_536L, parseHumanBytes("1.5 KiB"))
        assertEquals(77_154_222L, parseHumanBytes("73.58 MiB"))
        assertEquals(48_984_102_011L, parseHumanBytes("45.62 GiB"))
        assertEquals(1_506_330_930_053L, parseHumanBytes("1.37 TiB"))
        assertEquals(1_048_576L, parseHumanBytes("1 MB"))
    }

    @Test
    fun rejectsUnknownFormats() {
        assertNull(parseHumanBytes(""))
        assertNull(parseHumanBytes("n/a"))
        assertNull(parseHumanBytes("12 XB"))
    }

    @Test
    fun mapsMetricsResponse() {
        val body = """
            {"response":{"nodes":[{"nodeUuid":"u1","nodeName":"nl-ams-1","countryEmoji":"🇳🇱",
              "providerName":"unknown","usersOnline":2,
              "inboundsStats":[{"tag":"VLESS","upload":"45.62 GiB","download":"weird"}],
              "outboundsStats":[{"tag":"BLOCK","upload":"0","download":"0"}]}]}}
        """.trimIndent()
        val metrics = json.decodeFromString<NodesMetricsResponse>(body).response.nodes.map { NodeMetrics.from(it) }
        val node = metrics.single()
        assertEquals(2, node.usersOnline)
        assertEquals(48_984_102_011L, node.inbounds.single().uploadBytes)
        assertNull(node.inbounds.single().downloadBytes)
        assertEquals("weird", node.inbounds.single().downloadRaw)
        assertEquals(0L, node.outbounds.single().downloadBytes)
    }
}
