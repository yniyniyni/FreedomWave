package art.yniyniyni.freedomwave.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAErrorSystemCancel
import platform.LocalAuthentication.LAErrorUserCancel
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.resume

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator =
    remember { IosBiometricAuthenticator() }

private class IosBiometricAuthenticator : BiometricAuthenticator {

    override fun isAvailable(): Boolean {
        val context = LAContext()
        return context.canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = null
        )
    }

    override suspend fun authenticate(): BiometricResult =
        suspendCancellableCoroutine { continuation ->
            val context = LAContext()
            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = "Unlock FreedomWave"
            ) { success, error ->
                if (!continuation.isActive) return@evaluatePolicy
                when {
                    success -> continuation.resume(BiometricResult.Success)
                    error?.code?.toInt() == LAErrorUserCancel.toInt() ||
                    error?.code?.toInt() == LAErrorSystemCancel.toInt() ->
                        continuation.resume(BiometricResult.Cancelled)
                    else -> continuation.resume(
                        BiometricResult.Error(error?.localizedDescription ?: "Authentication failed")
                    )
                }
            }
        }
}
