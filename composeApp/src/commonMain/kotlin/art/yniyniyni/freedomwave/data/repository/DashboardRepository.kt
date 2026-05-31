package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.ApiError
import art.yniyniyni.freedomwave.data.api.service.DashboardService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.DashboardStats
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DashboardRepository(
    private val service: DashboardService,
    private val prefs: AppPreferences
) {
    suspend fun getStats(): Result<DashboardStats> = runCatching {
        val serverUrl = prefs.getServerUrl()
        coroutineScope {
            val statsDeferred = async { service.getSystemStats(serverUrl) }
            val recapDeferred = async { service.getRecap(serverUrl) }
            DashboardStats.from(statsDeferred.await().response, recapDeferred.await().response)
        }
    }.also { result ->
        if (result.exceptionOrNull() is ApiError.Unauthorized) {
            prefs.clearCredentials()
        }
    }
}
