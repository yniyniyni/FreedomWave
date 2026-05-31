package art.yniyniyni.freedomwave

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_DARK
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_LIGHT
import art.yniyniyni.freedomwave.ui.MainScreen
import art.yniyniyni.freedomwave.ui.feature.login.LoginScreen
import art.yniyniyni.freedomwave.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val prefs: AppPreferences = koinInject()
    val isLoggedIn by prefs.isLoggedIn.collectAsState(false)
    val themeMode by prefs.themeMode.collectAsState(AppPreferences.THEME_SYSTEM)
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        THEME_DARK  -> true
        THEME_LIGHT -> false
        else        -> systemDark
    }

    AppTheme(darkTheme = isDark) {
        if (isLoggedIn) {
            MainScreen()
        } else {
            LoginScreen()
        }
    }
}
