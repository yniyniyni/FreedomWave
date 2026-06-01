package art.yniyniyni.freedomwave.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
actual fun AppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) FwDarkScheme else FwLightScheme
    val status = if (darkTheme) FwStatusDark else FwStatusLight
    val jbm    = JetBrainsMono()
    CompositionLocalProvider(
        LocalFwStatus  provides status,
        LocalFwMonoFont provides jbm,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography  = fwTypography(),
            shapes      = FwShapes,
            content     = content,
        )
    }
}
