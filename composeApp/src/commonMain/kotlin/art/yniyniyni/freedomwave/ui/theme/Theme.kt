package art.yniyniyni.freedomwave.ui.theme

import androidx.compose.runtime.Composable

@Composable
expect fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
)
