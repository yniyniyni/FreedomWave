package art.yniyniyni.freedomwave.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import art.yniyniyni.freedomwave.ui.components.LocalTabAtRootReporter
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.nav_dashboard
import freedomwave.composeapp.generated.resources.nav_hosts
import freedomwave.composeapp.generated.resources.nav_nodes
import freedomwave.composeapp.generated.resources.nav_settings
import freedomwave.composeapp.generated.resources.nav_users
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

/** What the tab content animates on: the tab plus a reset counter (re-tap from a detail). */
private data class TabContent(val tab: MainTab, val resetKey: Int)

@Composable
fun MainScreen() {
    var selected by rememberSaveable { mutableStateOf(MainTab.DASHBOARD) }
    // Bumped when the active tab is re-tapped from a detail; forces the tab to recreate at its root.
    var resetKey by rememberSaveable { mutableStateOf(0) }
    // Whether the currently shown tab is at its root, reported by its navigation owner (if any).
    val atRoot = remember { mutableStateOf(true) }
    val reportAtRoot = remember { { v: Boolean -> atRoot.value = v } }
    // Tab-switch transition: -1 slide from the left, 1 slide from the right, 0 cross-fade.
    var transitionDir by remember { mutableStateOf(0) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                val tabEntries = remember { MainTab.entries }
                tabEntries.forEach { tab ->
                    val isSelected = selected == tab
                    val tabLabel = stringResource(tab.labelRes)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            val cur = selected
                            if (cur == tab) {
                                // Re-tap: from a detail, fade back to the root; at root, do nothing.
                                if (!atRoot.value) {
                                    transitionDir = 0
                                    resetKey++
                                    atRoot.value = true
                                }
                            } else {
                                // Slide directionally from a root; cross-fade from inside a detail.
                                transitionDir = when {
                                    !atRoot.value -> 0
                                    tab.ordinal > cur.ordinal -> 1
                                    else -> -1
                                }
                                selected = tab
                                // The target tab opens at its root; its owner refines this if needed.
                                atRoot.value = true
                            }
                        },
                        icon = {
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
        CompositionLocalProvider(LocalTabAtRootReporter provides reportAtRoot) {
            AnimatedContent(
                targetState = TabContent(selected, resetKey),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentKey = { "${it.tab}-${it.resetKey}" },
                transitionSpec = {
                    when (transitionDir) {
                        1 -> (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                        -1 -> (slideInHorizontally { -it } + fadeIn()) togetherWith
                            (slideOutHorizontally { it } + fadeOut())
                        else -> fadeIn() togetherWith fadeOut()
                    }
                },
                label = "tab",
            ) { state ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (state.tab) {
                        MainTab.DASHBOARD -> DashboardScreen()
                        MainTab.USERS     -> UsersScreen()
                        MainTab.NODES     -> NodesScreen()
                        MainTab.HOSTS     -> HostsScreen()
                        MainTab.SETTINGS  -> SettingsScreen()
                    }
                }
            }
        }
    }
}
