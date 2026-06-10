package art.yniyniyni.freedomwave.ui.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.common_cancel
import art.yniyniyni.freedomwave.resources.lock_biometric_subtitle
import art.yniyniyni.freedomwave.resources.lock_biometric_title
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.resources.stringResource
import kotlin.coroutines.resume

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    val context = LocalContext.current
    // CMP stringResource is composable-only; resolve prompt texts here and
    // hand them to the non-composable authenticator.
    val promptTitle    = stringResource(Res.string.lock_biometric_title)
    val promptSubtitle = stringResource(Res.string.lock_biometric_subtitle)
    val promptCancel   = stringResource(Res.string.common_cancel)
    return remember(context, promptTitle, promptSubtitle, promptCancel) {
        AndroidBiometricAuthenticator(
            activity       = context as FragmentActivity,
            promptTitle    = promptTitle,
            promptSubtitle = promptSubtitle,
            promptCancel   = promptCancel,
        )
    }
}

private class AndroidBiometricAuthenticator(
    private val activity: FragmentActivity,
    private val promptTitle: String,
    private val promptSubtitle: String,
    private val promptCancel: String,
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
                .setTitle(promptTitle)
                .setSubtitle(promptSubtitle)
                .setNegativeButtonText(promptCancel)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()

            continuation.invokeOnCancellation { prompt.cancelAuthentication() }
            prompt.authenticate(promptInfo)
        }
}
