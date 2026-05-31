package art.yniyniyni.freedomwave.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import art.yniyniyni.freedomwave.ui.feature.dashboard.DashboardScreen
import art.yniyniyni.freedomwave.ui.feature.hosts.HostsScreen
import art.yniyniyni.freedomwave.ui.feature.nodes.NodesScreen
import art.yniyniyni.freedomwave.ui.feature.settings.SettingsScreen
import art.yniyniyni.freedomwave.ui.feature.users.UsersScreen

private enum class MainTab(val label: String, val icon: String) {
    DASHBOARD("Dashboard", "📊"),
    USERS("Users",         "👥"),
    NODES("Nodes",         "🖥"),
    HOSTS("Hosts",         "🌐"),
    SETTINGS("Settings",   "⚙️"),
}

@Composable
fun MainScreen() {
    var selected by rememberSaveable { mutableStateOf(MainTab.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selected == tab,
                        onClick  = { selected = tab },
                        icon     = { Text(tab.icon) },
                        label    = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selected) {
                MainTab.DASHBOARD -> DashboardScreen()
                MainTab.USERS     -> UsersScreen()
                MainTab.NODES     -> NodesScreen()
                MainTab.HOSTS     -> HostsScreen()
                MainTab.SETTINGS  -> SettingsScreen()
            }
        }
    }
}
