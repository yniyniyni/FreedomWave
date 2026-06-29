package org.freedomwave.ui.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CancellationException

@Composable
actual fun BackGestureEffect(
    enabled: Boolean,
    onProgress: suspend (fraction: Float) -> Unit,
    onCommit: suspend () -> Unit,
    onCancel: suspend () -> Unit,
) {
    PredictiveBackHandler(enabled = enabled) { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                onProgress(backEvent.progress)
            }
            // Flow completed normally → gesture committed (finger released past threshold)
            onCommit()
        } catch (_: CancellationException) {
            // Flow cancelled → gesture abandoned (finger released below threshold)
            onCancel()
        }
    }
}
