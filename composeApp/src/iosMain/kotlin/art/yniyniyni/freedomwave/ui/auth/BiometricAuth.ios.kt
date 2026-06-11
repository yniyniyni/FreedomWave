package art.yniyniyni.freedomwave.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.lock_biometric_failed
import freedomwave.composeapp.generated.resources.lock_biometric_title
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.resources.stringResource
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAErrorSystemCancel
import platform.LocalAuthentication.LAErrorUserCancel
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.resume

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    // CMP stringResource is composable-only; resolve prompt texts here and
    // hand them to the non-composable authenticator.
    val reason        = stringResource(Res.string.lock_biometric_title)
    val fallbackError = stringResource(Res.string.lock_biometric_failed)
    return remember(reason, fallbackError) {
        IosBiometricAuthenticator(reason, fallbackError)
    }
}

private class IosBiometricAuthenticator(
    private val reason: String,
    private val fallbackError: String,
) : BiometricAuthenticator {

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
                localizedReason = reason
            ) { success, error ->
                if (!continuation.isActive) return@evaluatePolicy
                when {
                    success -> continuation.resume(BiometricResult.Success)
                    error?.code?.toInt() == LAErrorUserCancel.toInt() ||
                    error?.code?.toInt() == LAErrorSystemCancel.toInt() ->
                        continuation.resume(BiometricResult.Cancelled)
                    else -> continuation.resume(
                        BiometricResult.Error(error?.localizedDescription ?: fallbackError)
                    )
                }
            }
        }
}
