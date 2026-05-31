package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.service.AuthService
import art.yniyniyni.freedomwave.data.store.AppPreferences

class AuthRepository(
    private val authService: AuthService,
    private val prefs: AppPreferences
) {
    suspend fun login(serverUrl: String, username: String, password: String): Result<Unit> =
        runCatching {
            val response = authService.login(serverUrl, username, password)
            prefs.saveCredentials(serverUrl, response.response.accessToken)
        }

    suspend fun logout() {
        prefs.clearCredentials()
    }
}
