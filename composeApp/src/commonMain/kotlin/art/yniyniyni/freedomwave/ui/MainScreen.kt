package art.yniyniyni.freedomwave.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import art.yniyniyni.freedomwave.ui.feature.dashboard.DashboardScreen
import art.yniyniyni.freedomwave.ui.feature.hosts.HostsScreen
import art.yniyniyni.freedomwave.ui.feature.nodes.NodesScreen
import art.yniyniyni.freedomwave.ui.feature.settings.SettingsScreen
import art.yniyniyni.freedomwave.ui.feature.users.UsersScreen

private enum class MainTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Rounded.SpaceDashboard),
    USERS    ("Users",     Icons.Rounded.Group),
    NODES    ("Nodes",     Icons.Rounded.Dns),
    HOSTS    ("Hosts",     Icons.Rounded.Public),
    SETTINGS ("Settings",  Icons.Rounded.Settings),
}

@Composable
fun MainScreen() {
    var selected by rememberSaveable { mutableStateOf(MainTab.DASHBOARD) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                MainTab.entries.forEach { tab ->
                    val isSelected = selected == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick  = { selected = tab },
                        icon     = {
                            Icon(
                                imageVector        = tab.icon,
                                contentDescription = tab.label,
                            )
                        },
                        label  = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor       = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor    = MaterialTheme.colorScheme.primary,
                            selectedTextColor    = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
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
