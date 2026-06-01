package art.yniyniyni.freedomwave.ui.navigation

import androidx.compose.runtime.Composable

/**
 * Predictive back gesture handler.
 *
 * [onProgress] is called with a 0→1 fraction as the user drags the back gesture.
 * [onCommit]  is called when the gesture is released past the threshold (back confirmed).
 * [onCancel]  is called when the gesture is released below the threshold (back cancelled).
 *
 * All three lambdas are suspend so the Android actual can call SeekableTransitionState
 * functions (which are suspend) directly from inside the PredictiveBackHandler coroutine.
 *
 * iOS actual: no-op — iOS back navigation is handled by native platform gestures.
 */
@Composable
expect fun BackGestureEffect(
    enabled: Boolean,
    onProgress: suspend (fraction: Float) -> Unit,
    onCommit: suspend () -> Unit,
    onCancel: suspend () -> Unit,
)
