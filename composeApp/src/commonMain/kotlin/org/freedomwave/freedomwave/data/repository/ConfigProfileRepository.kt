package org.freedomwave.data.repository

import org.freedomwave.data.api.service.ConfigProfileService
import org.freedomwave.data.store.AppPreferences
import org.freedomwave.domain.model.ConfigProfile

class ConfigProfileRepository(
    private val service: ConfigProfileService,
    private val prefs: AppPreferences,
) {
    suspend fun getProfiles(): Result<List<ConfigProfile>> = api {
        service.getConfigProfiles().response.configProfiles.map { ConfigProfile.from(it) }
    }

    suspend fun getSecretKey(): Result<String> = api {
        service.getPubKey().response.pubKey
    }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.clearOnUnauthorized(prefs)
}
