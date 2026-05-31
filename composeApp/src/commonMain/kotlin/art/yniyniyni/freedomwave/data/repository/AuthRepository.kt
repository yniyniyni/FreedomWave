package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.service.AuthService
import art.yniyniyni.freedomwave.data.store.AppPreferences

class AuthRepository(
    private val authService: AuthService,
    private val prefs: AppPreferences
) {
    suspend fun saveApiKey(serverUrl: String, apiKey: String): Result<Unit> = runCatching {
        prefs.saveApiKey(serverUrl, apiKey)
        authService.verifyConnection(serverUrl)
    }.onFailure {
        prefs.clearCredentials()
    }

    suspend fun logout() {
        prefs.clearCredentials()
    }
}
