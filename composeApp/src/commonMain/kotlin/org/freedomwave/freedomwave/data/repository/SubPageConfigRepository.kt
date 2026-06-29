package org.freedomwave.data.repository

import org.freedomwave.data.api.service.SubPageConfigService
import org.freedomwave.data.store.AppPreferences
import org.freedomwave.domain.model.SubPageConfig

class SubPageConfigRepository(
    private val service: SubPageConfigService,
    private val prefs: AppPreferences,
) {
    suspend fun getConfigs(): Result<List<SubPageConfig>> = runCatching {
        service.getConfigs().response.configs.map { SubPageConfig(it.uuid, it.name) }
    }.clearOnUnauthorized(prefs)
}
