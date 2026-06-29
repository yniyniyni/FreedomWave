package org.freedomwave.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import freedomwave.composeapp.generated.resources.JetBrainsMono
import freedomwave.composeapp.generated.resources.JetBrainsMonoItalic
import freedomwave.composeapp.generated.resources.Manrope
import freedomwave.composeapp.generated.resources.MartianMono
import freedomwave.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun MartianMono() = FontFamily(
    Font(Res.font.MartianMono, FontWeight.Medium),
    Font(Res.font.MartianMono, FontWeight.SemiBold),
    Font(Res.font.MartianMono, FontWeight.Bold),
)

@Composable
fun Manrope() = FontFamily(
    Font(Res.font.Manrope, FontWeight.Normal),
    Font(Res.font.Manrope, FontWeight.Medium),
    Font(Res.font.Manrope, FontWeight.SemiBold),
    Font(Res.font.Manrope, FontWeight.Bold),
)

@Composable
fun JetBrainsMono() = FontFamily(
    Font(Res.font.JetBrainsMono, FontWeight.Normal),
    Font(Res.font.JetBrainsMono, FontWeight.Medium),
    Font(Res.font.JetBrainsMonoItalic, FontWeight.Normal, FontStyle.Italic),
)

val LocalFwMonoFont = staticCompositionLocalOf<FontFamily> { FontFamily.Monospace }

@Composable
fun fwTypography(): Typography {
    val mm = MartianMono()
    val mr = Manrope()
    return Typography(
        displayLarge   = TextStyle(fontFamily = mm, fontWeight = FontWeight.Bold,     fontSize = 52.sp, lineHeight = 60.sp, letterSpacing = (-2.5).sp),
        displayMedium  = TextStyle(fontFamily = mm, fontWeight = FontWeight.Bold,     fontSize = 42.sp, lineHeight = 50.sp, letterSpacing = (-2.0).sp),
        displaySmall   = TextStyle(fontFamily = mm, fontWeight = FontWeight.Bold,     fontSize = 33.sp, lineHeight = 42.sp, letterSpacing = (-1.6).sp),
        headlineLarge  = TextStyle(fontFamily = mm, fontWeight = FontWeight.Bold,     fontSize = 29.sp, lineHeight = 38.sp, letterSpacing = (-1.4).sp),
        headlineMedium = TextStyle(fontFamily = mm, fontWeight = FontWeight.Bold,     fontSize = 25.sp, lineHeight = 34.sp, letterSpacing = (-1.1).sp),
        headlineSmall  = TextStyle(fontFamily = mm, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 30.sp, letterSpacing = (-1.0).sp),
        titleLarge     = TextStyle(fontFamily = mm, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = (-0.9).sp),
        titleMedium    = TextStyle(fontFamily = mr, fontWeight = FontWeight.Bold,     fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
        titleSmall     = TextStyle(fontFamily = mr, fontWeight = FontWeight.Bold,     fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        bodyLarge      = TextStyle(fontFamily = mr, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium     = TextStyle(fontFamily = mr, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall      = TextStyle(fontFamily = mr, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge     = TextStyle(fontFamily = mr, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        labelMedium    = TextStyle(fontFamily = mr, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
        labelSmall     = TextStyle(fontFamily = mr, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    )
}
