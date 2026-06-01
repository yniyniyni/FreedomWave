package art.yniyniyni.freedomwave.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val FwDarkScheme = darkColorScheme(
    primary                = Color(0xFF2FE0C9),
    onPrimary              = Color(0xFF00352E),
    primaryContainer       = Color(0xFF0A5249),
    onPrimaryContainer     = Color(0xFF8DF7E9),
    secondary              = Color(0xFFC2CCD6),
    onSecondary            = Color(0xFF0B0E13),
    secondaryContainer     = Color(0xFF222A34),
    onSecondaryContainer   = Color(0xFFE7ECF3),
    tertiary               = Color(0xFF6FB7FF),
    onTertiary             = Color(0xFF062744),
    background             = Color(0xFF0B0E13),
    onBackground           = Color(0xFFE7ECF3),
    surface                = Color(0xFF0B0E13),
    onSurface              = Color(0xFFE7ECF3),
    surfaceVariant         = Color(0xFF222A34),
    onSurfaceVariant       = Color(0xFF97A4B2),
    surfaceContainerLowest = Color(0xFF080A0E),
    surfaceContainerLow    = Color(0xFF11151B),
    surfaceContainer       = Color(0xFF151A21),
    surfaceContainerHigh   = Color(0xFF1B212A),
    surfaceContainerHighest= Color(0xFF222A34),
    outline                = Color(0xFF38424F),
    outlineVariant         = Color(0xFF222A34),
    error                  = Color(0xFFFF6B7D),
    onError                = Color(0xFF3A0008),
)

val FwLightScheme = lightColorScheme(
    primary                = Color(0xFF0A7E72),
    onPrimary              = Color(0xFFFFFFFF),
    primaryContainer       = Color(0xFFB9F5EA),
    onPrimaryContainer     = Color(0xFF00251F),
    secondary              = Color(0xFF51606E),
    onSecondary            = Color(0xFFFFFFFF),
    secondaryContainer     = Color(0xFFDCE4EC),
    onSecondaryContainer   = Color(0xFF16202A),
    tertiary               = Color(0xFF1F6FD6),
    onTertiary             = Color(0xFFFFFFFF),
    background             = Color(0xFFF6F8FA),
    onBackground           = Color(0xFF0E141B),
    surface                = Color(0xFFFFFFFF),
    onSurface              = Color(0xFF0E141B),
    surfaceVariant         = Color(0xFFDDE5EC),
    onSurfaceVariant       = Color(0xFF51606E),
    surfaceContainerLow    = Color(0xFFF2F5F8),
    surfaceContainer       = Color(0xFFEDF1F5),
    surfaceContainerHigh   = Color(0xFFE5EBF1),
    surfaceContainerHighest= Color(0xFFDDE5EC),
    outline                = Color(0xFFB7C2CD),
    outlineVariant         = Color(0xFFDCE3EA),
    error                  = Color(0xFFD23E52),
    onError                = Color(0xFFFFFFFF),
)

data class FwStatus(
    val online: Color,
    val warning: Color,
    val offline: Color,
    val neutral: Color,
)

val FwStatusDark  = FwStatus(Color(0xFF45D483), Color(0xFFF4B740), Color(0xFFFF6B7D), Color(0xFF97A4B2))
val FwStatusLight = FwStatus(Color(0xFF1E9E5A), Color(0xFFB5790C), Color(0xFFD23E52), Color(0xFF51606E))

val LocalFwStatus = staticCompositionLocalOf { FwStatusDark }
