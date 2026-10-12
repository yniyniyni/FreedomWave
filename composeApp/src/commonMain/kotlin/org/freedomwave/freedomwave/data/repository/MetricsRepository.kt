package org.freedomwave.data.repository

import org.freedomwave.data.api.service.DashboardService
import org.freedomwave.data.store.AppPreferences
import org.freedomwave.domain.model.NodeMetrics

class MetricsRepository(
    private val service: DashboardService,
    private val prefs: AppPreferences
) {
    suspend fun getNodesMetrics(): Result<List<NodeMetrics>> =
        runCatching {
            service.getNodesMetrics().response.nodes.map { NodeMetrics.from(it) }
        }.clearOnUnauthorized(prefs)
}
