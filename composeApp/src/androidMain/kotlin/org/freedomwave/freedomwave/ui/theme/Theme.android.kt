package org.freedomwave.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun AppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) FwDarkScheme else FwLightScheme
    val status = if (darkTheme) FwStatusDark else FwStatusLight
    val jbm    = JetBrainsMono()

    // Edge-to-edge keeps the system bars transparent, so sync the icon tint to the app's own
    // theme (not the OS dark-mode setting): light icons on dark backgrounds, dark on light.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            view.context.findActivity()?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

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
