package art.yniyniyni.freedomwave.ui.auth

import androidx.compose.runtime.Composable

sealed class BiometricResult {
    object Success      : BiometricResult()
    object Cancelled    : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}

interface BiometricAuthenticator {
    fun isAvailable(): Boolean
    suspend fun authenticate(): BiometricResult
}

@Composable
expect fun rememberBiometricAuthenticator(): BiometricAuthenticator
