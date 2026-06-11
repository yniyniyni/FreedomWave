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
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.nav_dashboard
import art.yniyniyni.freedomwave.resources.nav_hosts
import art.yniyniyni.freedomwave.resources.nav_nodes
import art.yniyniyni.freedomwave.resources.nav_settings
import art.yniyniyni.freedomwave.resources.nav_users
import art.yniyniyni.freedomwave.ui.feature.dashboard.DashboardScreen
import art.yniyniyni.freedomwave.ui.feature.hosts.HostsScreen
import art.yniyniyni.freedomwave.ui.feature.nodes.NodesScreen
import art.yniyniyni.freedomwave.ui.feature.settings.SettingsScreen
import art.yniyniyni.freedomwave.ui.feature.users.UsersScreen
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private enum class MainTab(val labelRes: StringResource, val icon: ImageVector) {
    DASHBOARD(Res.string.nav_dashboard, Icons.Rounded.SpaceDashboard),
    USERS    (Res.string.nav_users,     Icons.Rounded.Group),
    NODES    (Res.string.nav_nodes,     Icons.Rounded.Dns),
    HOSTS    (Res.string.nav_hosts,     Icons.Rounded.Public),
    SETTINGS (Res.string.nav_settings,  Icons.Rounded.Settings),
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
                    val tabLabel = stringResource(tab.labelRes)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick  = { selected = tab },
                        icon     = {
                            Icon(
                                imageVector        = tab.icon,
                                contentDescription = tabLabel,
                            )
                        },
                        label  = { Text(tabLabel, style = MaterialTheme.typography.labelSmall) },
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
