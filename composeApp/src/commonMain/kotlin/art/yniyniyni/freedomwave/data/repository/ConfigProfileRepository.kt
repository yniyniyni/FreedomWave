package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.service.ConfigProfileService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.ConfigProfile

class ConfigProfileRepository(
    private val service: ConfigProfileService,
    private val prefs: AppPreferences,
) {
    suspend fun getProfiles(): Result<List<ConfigProfile>> = api {
        service.getConfigProfiles(prefs.getServerUrl()).response.configProfiles.map { ConfigProfile.from(it) }
    }

    suspend fun getSecretKey(): Result<String> = api {
        service.getPubKey(prefs.getServerUrl()).response.pubKey
    }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.also { it.clearOnUnauthorized(prefs) }
}
