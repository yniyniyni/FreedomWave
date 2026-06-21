package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.service.DashboardService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.DashboardStats
import art.yniyniyni.freedomwave.util.parsePrettyBytes
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.TimeZone

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
            // "Today" traffic (and its delta vs yesterday) comes from the dedicated
            // bandwidth endpoint; the daily sparkline's last bucket would read ~0 in the
            // early hours of a new day. Pass the device timezone so "today" matches the
            // calendar day the user sees on their phone (the backend would otherwise use
            // its own configured tz). Tolerate failure — the rest of the dashboard should
            // still load.
            val tz = TimeZone.currentSystemDefault().id
            val bandwidthDeferred = async { runCatching { service.getBandwidthStats(serverUrl, tz) }.getOrNull() }

            val base = DashboardStats.from(statsDeferred.await().response, recapDeferred.await().response)

            // online + total node counts otherwise come from two different endpoints
            // (system-stats vs recap) and can disagree (e.g. "5 / 4"); derive both from
            // the single nodes list when it is available, falling back to the base stats.
            val nodes = nodesDeferred.await().getOrNull()
            val withNodes = if (nodes != null) {
                base.copy(onlineNodes = nodes.count { it.isOnline }, totalNodes = nodes.size)
            } else {
                base
            }

            val lastTwoDays = bandwidthDeferred.await()?.response?.lastTwoDays
            if (lastTwoDays != null) {
                val today = parsePrettyBytes(lastTwoDays.current)
                val yesterday = parsePrettyBytes(lastTwoDays.previous)
                val delta = if (yesterday > 0) {
                    ((today - yesterday).toDouble() / yesterday * 100).toInt()
                } else {
                    null
                }
                withNodes.copy(todayBytes = today, todayDeltaPercent = delta)
            } else {
                withNodes
            }
        }
    }.also { it.clearOnUnauthorized(prefs) }
}
