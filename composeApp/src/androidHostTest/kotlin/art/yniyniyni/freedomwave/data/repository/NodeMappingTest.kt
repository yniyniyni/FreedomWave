package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.dto.NodeDto
import art.yniyniyni.freedomwave.data.api.dto.NodeSystemDto
import art.yniyniyni.freedomwave.data.api.dto.NodeSystemInfoDto
import art.yniyniyni.freedomwave.data.api.dto.NodeSystemStatsDto
import art.yniyniyni.freedomwave.data.api.dto.NodeVersionsDto
import art.yniyniyni.freedomwave.domain.model.Node
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NodeMappingTest {

    private fun dto(
        cpus: Int = 2,
        loadAvg: List<Double> = listOf(1.0, 0.5, 0.25),
        memoryUsed: Long = 500,
        memoryTotal: Long = 1000,
        withSystem: Boolean = true,
    ) = NodeDto(
        uuid = "u1",
        name = "node-1",
        address = "1.2.3.4",
        port = 5555,
        isConnected = true,
        isDisabled = false,
        isConnecting = false,
        isTrafficTrackingActive = true,
        trafficResetDay = 4,
        trafficLimitBytes = 900,
        trafficUsedBytes = 90,
        notifyPercent = 90,
        viewPosition = 1,
        countryCode = "IT",
        consumptionMultiplier = 1.0,
        usersOnline = 3,
        xrayUptime = 600.0,
        versions = NodeVersionsDto(xray = "26.3.27", node = "2.7.0"),
        lastStatusChange = "2026-06-09T00:45:00.000Z",
        createdAt = "2024-01-01T00:00:00.000Z",
        updatedAt = "2024-01-02T00:00:00.000Z",
        system = if (withSystem) NodeSystemDto(
            info = NodeSystemInfoDto(
                arch = "x86_64", cpus = cpus, cpuModel = "AMD EPYC 7713",
                memoryTotal = memoryTotal, hostname = "host", platform = "linux",
            ),
            stats = NodeSystemStatsDto(
                memoryFree = memoryTotal - memoryUsed, memoryUsed = memoryUsed,
                uptime = 1000.0, loadAvg = loadAvg,
            ),
        ) else null,
    )

    @Test
    fun `maps versions online count and xray uptime`() {
        val node = Node.from(dto())
        assertEquals(3, node.usersOnline)
        assertEquals("26.3.27", node.xrayVersion)
        assertEquals("2.7.0", node.nodeVersion)
        assertEquals(600L, node.xrayUptimeSeconds)
        assertEquals(90, node.notifyPercent)
        assertEquals(4, node.trafficResetDay)
        assertEquals("AMD EPYC 7713", node.cpuModel)
    }

    @Test
    fun `cpuLoadPercent is loadAvg over cpus times 100`() {
        val node = Node.from(dto(cpus = 2, loadAvg = listOf(1.0, 0.5, 0.25)))
        assertEquals(50f, node.cpuLoadPercent)
    }

    @Test
    fun `cpuLoadPercent clamps to 100`() {
        val node = Node.from(dto(cpus = 1, loadAvg = listOf(4.0)))
        assertEquals(100f, node.cpuLoadPercent)
    }

    @Test
    fun `cpuLoadPercent is null without system`() {
        assertNull(Node.from(dto(withSystem = false)).cpuLoadPercent)
    }

    @Test
    fun `memoryUsedPercent computes from used over total`() {
        val node = Node.from(dto(memoryUsed = 500, memoryTotal = 1000))
        assertEquals(50f, node.memoryUsedPercent)
    }
}
