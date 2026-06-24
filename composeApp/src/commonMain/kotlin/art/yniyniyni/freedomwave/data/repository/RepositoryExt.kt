package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.ApiError
import art.yniyniyni.freedomwave.data.store.AppPreferences

/**
 * Clears stored credentials when this [Result] carries an [ApiError.Unauthorized] so the
 * navigation layer picks up the `isLoggedIn` change and routes back to the login screen.
 */
internal suspend fun <T> Result<T>.clearOnUnauthorized(prefs: AppPreferences): Result<T> {
    if (exceptionOrNull() is ApiError.Unauthorized) {
        prefs.clearCredentials()
        // Forced logout: drop biometric unlock too, mirroring an explicit log out.
        prefs.saveBiometricEnabled(false)
    }
    return this
}
