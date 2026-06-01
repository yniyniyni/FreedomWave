package art.yniyniyni.freedomwave.ui.feature.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.ui.auth.BiometricAuthenticator
import art.yniyniyni.freedomwave.ui.auth.BiometricResult
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    authenticator: BiometricAuthenticator,
    onUnlocked: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Skip lock entirely if biometrics unavailable on this device
    LaunchedEffect(Unit) {
        if (!authenticator.isAvailable()) {
            onUnlocked()
            return@LaunchedEffect
        }
        // Auto-trigger on first show
        triggerAuth(
            authenticator = authenticator,
            onStart  = { isAuthenticating = true },
            onFinish = { isAuthenticating = false },
            onError  = { errorMessage = it },
            onSuccess = onUnlocked
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "FreedomWave",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Locked",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(48.dp))

            if (isAuthenticating) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else {
                errorMessage?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                }
                Button(
                    onClick = {
                        scope.launch {
                            triggerAuth(
                                authenticator = authenticator,
                                onStart   = { isAuthenticating = true; errorMessage = null },
                                onFinish  = { isAuthenticating = false },
                                onError   = { errorMessage = it },
                                onSuccess = onUnlocked
                            )
                        }
                    }
                ) {
                    Text("Unlock")
                }
            }
        }
    }
}

private suspend fun triggerAuth(
    authenticator: BiometricAuthenticator,
    onStart: () -> Unit,
    onFinish: () -> Unit,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    onStart()
    when (val result = authenticator.authenticate()) {
        BiometricResult.Success    -> onSuccess()
        BiometricResult.Cancelled  -> { /* user dismissed — stay on lock screen */ }
        is BiometricResult.Error   -> onError(result.message)
    }
    onFinish()
}
