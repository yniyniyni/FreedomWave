package org.freedomwave.data.repository

import org.freedomwave.data.api.ApiError
import org.freedomwave.data.api.service.AuthService
import org.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProviders
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

/** True only for a well-formed `https://<host>...` URL. Plain http would leak the key in clear. */
internal fun isHttpsUrl(raw: String): Boolean {
    val url = raw.trim()
    val prefix = "https://"
    if (!url.startsWith(prefix, ignoreCase = true)) return false
    val host = url.substring(prefix.length).substringBefore('/').substringBefore('?')
    return host.isNotBlank()
}

class AuthRepository(
    private val authService: AuthService,
    private val prefs: AppPreferences,
    private val client: HttpClient
) {
    suspend fun saveApiKey(serverUrl: String, apiKey: String): Result<Unit> = runCatching {
        val url = serverUrl.trim().trimEnd('/')
        if (!isHttpsUrl(url)) throw ApiError.InvalidServerUrl()
        val key = apiKey.trim()

        // Verify BEFORE persisting: a bad URL/key must not flip isLoggedIn or flash MainScreen,
        // and a failed "Change key" must leave the existing session untouched.
        authService.verifyConnection(url, key)

        prefs.saveApiKey(url, key)
        // Drop any cached (old) token so the next real request loads the key we just saved.
        clearBearerTokenCache()
    }

    suspend fun logout() {
        prefs.clearCredentials()
        // Disable biometric unlock on logout — the next account shouldn't inherit it, and there
        // are no credentials left to protect until the user opts in again.
        prefs.saveBiometricEnabled(false)
        clearBearerTokenCache()
    }

    /** Evict the cached BearerTokens so a subsequent request reloads the key from prefs. */
    private fun clearBearerTokenCache() {
        client.authProviders
            .filterIsInstance<BearerAuthProvider>()
            .forEach { it.clearToken() }
    }
}
