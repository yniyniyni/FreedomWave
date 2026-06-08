package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.ApiError
import art.yniyniyni.freedomwave.data.api.service.DashboardService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.DashboardStats
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DashboardRepository(
    private val service: DashboardService,
    private val nodeRepository: NodeRepository,
    private val prefs: AppPreferences
) {
    suspend fun getStats(): Result<DashboardStats> = runCatching {
        val serverUrl = prefs.getServerUrl()
        coroutineScope {
            val statsDeferred = async { service.getSystemStats(serverUrl) }
            val recapDeferred = async { service.getRecap(serverUrl) }
            val nodesDeferred = async { nodeRepository.getNodes() }
            val base = DashboardStats.from(statsDeferred.await().response, recapDeferred.await().response)
            // online + total node counts otherwise come from two different endpoints
            // (system-stats vs recap) and can disagree (e.g. "5 / 4"); derive both from
            // the single nodes list when it is available, falling back to the base stats.
            val nodes = nodesDeferred.await().getOrNull()
            if (nodes != null) {
                base.copy(onlineNodes = nodes.count { it.isOnline }, totalNodes = nodes.size)
            } else {
                base
            }
        }
    }.also { result ->
        if (result.exceptionOrNull() is ApiError.Unauthorized) {
            prefs.clearCredentials()
        }
    }
}
