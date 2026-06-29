package org.freedomwave.ui.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun BackGestureEffect(
    enabled: Boolean,
    onProgress: suspend (fraction: Float) -> Unit,
    onCommit: suspend () -> Unit,
    onCancel: suspend () -> Unit,
) {
    // iOS: back navigation is provided by the native swipe-from-edge gesture via UIKit/SwiftUI.
}
