package art.yniyniyni.freedomwave.ui.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    val context = LocalContext.current
    return remember(context) {
        AndroidBiometricAuthenticator(context as FragmentActivity)
    }
}

private class AndroidBiometricAuthenticator(
    private val activity: FragmentActivity
) : BiometricAuthenticator {

    override fun isAvailable(): Boolean {
        val manager = BiometricManager.from(activity)
        val result = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    override suspend fun authenticate(): BiometricResult =
        suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (continuation.isActive) continuation.resume(BiometricResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (!continuation.isActive) return
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> continuation.resume(BiometricResult.Cancelled)
                        else -> continuation.resume(BiometricResult.Error(errString.toString()))
                    }
                }
            }

            val prompt = BiometricPrompt(activity, executor, callback)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock FreedomWave")
                .setSubtitle("Authenticate to continue")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()

            continuation.invokeOnCancellation { prompt.cancelAuthentication() }
            prompt.authenticate(promptInfo)
        }
}
