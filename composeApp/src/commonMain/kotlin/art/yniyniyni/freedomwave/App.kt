package art.yniyniyni.freedomwave

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.ui.MainScreen
import art.yniyniyni.freedomwave.ui.feature.login.LoginScreen
import art.yniyniyni.freedomwave.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val prefs: AppPreferences = koinInject()
    val isLoggedIn by prefs.isLoggedIn.collectAsState(false)

    AppTheme {
        if (isLoggedIn) {
            MainScreen()
        } else {
            LoginScreen()
        }
    }
}
