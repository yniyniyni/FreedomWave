package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.ApiError
import art.yniyniyni.freedomwave.data.api.dto.BandwidthNodesData
import art.yniyniyni.freedomwave.data.api.service.BandwidthService
import art.yniyniyni.freedomwave.data.store.AppPreferences

class BandwidthRepository(
    private val service: BandwidthService,
    private val prefs: AppPreferences
) {
    suspend fun getNodesStats(start: String, end: String): Result<BandwidthNodesData> =
        runCatching {
            service.getNodesStats(prefs.getServerUrl(), start, end).response
        }.also { result ->
            if (result.exceptionOrNull() is ApiError.Unauthorized) prefs.clearCredentials()
        }
}
