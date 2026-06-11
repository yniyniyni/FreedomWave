package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.service.AuthService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProviders
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

class AuthRepository(
    private val authService: AuthService,
    private val prefs: AppPreferences,
    private val client: HttpClient
) {
    suspend fun saveApiKey(serverUrl: String, apiKey: String): Result<Unit> = runCatching {
        prefs.saveApiKey(serverUrl, apiKey)
        // The Bearer plugin caches the token from the first request; drop it so the very
        // next call re-runs loadTokens and picks up the key we just saved (fixes "Change key").
        clearBearerTokenCache()
        authService.verifyConnection(serverUrl)
    }.onFailure {
        prefs.clearCredentials()
        clearBearerTokenCache()
    }

    suspend fun logout() {
        prefs.clearCredentials()
        clearBearerTokenCache()
    }

    /** Evict the cached BearerTokens so a subsequent request reloads the key from prefs. */
    private fun clearBearerTokenCache() {
        client.authProviders
            .filterIsInstance<BearerAuthProvider>()
            .forEach { it.clearToken() }
    }
}
