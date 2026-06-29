package org.freedomwave.ui.feature.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import org.freedomwave.ui.components.WaveLoader
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
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_brand_freedom
import freedomwave.composeapp.generated.resources.common_brand_wave
import freedomwave.composeapp.generated.resources.lock_locked
import freedomwave.composeapp.generated.resources.lock_unlock
import org.freedomwave.ui.auth.BiometricAuthenticator
import org.freedomwave.ui.auth.BiometricResult
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun LockScreen(
    authenticator: BiometricAuthenticator,
    onUnlocked: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!authenticator.isAvailable()) {
            onUnlocked()
            return@LaunchedEffect
        }
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
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    stringResource(Res.string.common_brand_freedom),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    stringResource(Res.string.common_brand_wave),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(Res.string.lock_locked),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(48.dp))

            if (isAuthenticating) {
                WaveLoader(modifier = Modifier.size(width = 70.dp, height = 48.dp))
            } else {
                errorMessage?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
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
                    },
                    shape = RoundedCornerShape(percent = 50),
                ) {
                    Text(stringResource(Res.string.lock_unlock))
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
        BiometricResult.Cancelled  -> { }
        is BiometricResult.Error   -> onError(result.message)
    }
    onFinish()
}
