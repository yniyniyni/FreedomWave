package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.dto.BandwidthNodesData
import art.yniyniyni.freedomwave.data.api.service.BandwidthService
import art.yniyniyni.freedomwave.data.store.AppPreferences

class BandwidthRepository(
    private val service: BandwidthService,
    private val prefs: AppPreferences
) {
    suspend fun getNodesStats(start: String, end: String): Result<BandwidthNodesData> =
        runCatching {
            service.getNodesStats(start, end).response
        }.clearOnUnauthorized(prefs)

    suspend fun getUserStats(uuid: String, start: String, end: String): Result<BandwidthNodesData> =
        runCatching {
            service.getUserStats(uuid, start, end).response
        }.clearOnUnauthorized(prefs)
}
