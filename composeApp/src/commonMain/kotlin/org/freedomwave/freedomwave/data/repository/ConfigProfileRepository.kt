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
        // Field is `secretKey` on 3.x and `pubKey` on 2.8.x; same value either way.
        service.getNodeSecretKey().response.nodeSecretKey.orEmpty()
    }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.clearOnUnauthorized(prefs)
}
