package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.service.SubPageConfigService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.SubPageConfig

class SubPageConfigRepository(
    private val service: SubPageConfigService,
    private val prefs: AppPreferences,
) {
    suspend fun getConfigs(): Result<List<SubPageConfig>> = runCatching {
        service.getConfigs(prefs.getServerUrl()).response.configs.map { SubPageConfig(it.uuid, it.name) }
    }.also { it.clearOnUnauthorized(prefs) }
}
