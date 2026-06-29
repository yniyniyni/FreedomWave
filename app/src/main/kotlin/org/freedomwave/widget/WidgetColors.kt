package org.freedomwave.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

/**
 * Fixed FreedomWave brand palette for the widget, mapped from the design system's
 * dark-first tokens. The widget is intentionally dark-first (ink card + one aqua
 * signal) regardless of the launcher's day/night skin, so these are constant.
 */
object WidgetColors {
    /** surfaceContainer — the ink card the widget sits on (ink-800). */
    val surface = ColorProvider(Color(0xFF151A21))
    /** Primary text (ink-050). */
    val onSurface = ColorProvider(Color(0xFFE7ECF3))
    /** Muted labels / secondary text (ink-200). */
    val onSurfaceVariant = ColorProvider(Color(0xFF97A4B2))
    /** The single signal-aqua brand accent. */
    val aqua = ColorProvider(Color(0xFF2FE0C9))
    /** Status: online / all-healthy. */
    val online = ColorProvider(Color(0xFF45D483))
    /** Status: degraded / connecting. */
    val warn = ColorProvider(Color(0xFFF4B740))
    /** Status: offline / error. */
    val down = ColorProvider(Color(0xFFFF6B7D))
}
