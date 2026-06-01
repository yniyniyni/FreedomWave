package art.yniyniyni.freedomwave

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_DARK
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_LIGHT
import art.yniyniyni.freedomwave.ui.MainScreen
import art.yniyniyni.freedomwave.ui.auth.rememberBiometricAuthenticator
import art.yniyniyni.freedomwave.ui.feature.lock.LockScreen
import art.yniyniyni.freedomwave.ui.feature.login.LoginScreen
import art.yniyniyni.freedomwave.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val prefs: AppPreferences = koinInject()
    val isLoggedIn       by prefs.isLoggedIn.collectAsState(false)
    val themeMode        by prefs.themeMode.collectAsState(AppPreferences.THEME_SYSTEM)
    val biometricEnabled by prefs.biometricEnabled.collectAsState(false)
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        THEME_DARK  -> true
        THEME_LIGHT -> false
        else        -> systemDark
    }

    // Tracks whether the user has authenticated in this process session.
    // rememberSaveable survives recomposition + config changes but NOT process restart,
    // so the lock reappears on every cold launch.
    var isUnlocked by rememberSaveable { mutableStateOf(false) }

    AppTheme(darkTheme = isDark) {
        when {
            biometricEnabled && isLoggedIn && !isUnlocked -> {
                val authenticator = rememberBiometricAuthenticator()
                LockScreen(
                    authenticator = authenticator,
                    onUnlocked    = { isUnlocked = true }
                )
            }
            isLoggedIn -> MainScreen()
            else       -> LoginScreen()
        }
    }
}
