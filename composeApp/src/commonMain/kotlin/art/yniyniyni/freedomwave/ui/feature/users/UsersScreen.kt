@file:OptIn(ExperimentalTransitionApi::class)

package art.yniyniyni.freedomwave.ui.feature.users

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import art.yniyniyni.freedomwave.ui.navigation.BackGestureEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import art.yniyniyni.freedomwave.ui.components.FwDetailTopBar
import art.yniyniyni.freedomwave.ui.components.FwTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.domain.model.User
import art.yniyniyni.freedomwave.domain.model.UserStatus
import art.yniyniyni.freedomwave.ui.components.ShimmerList
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import art.yniyniyni.freedomwave.ui.theme.LocalFwStatus
import art.yniyniyni.freedomwave.util.formatBytes
import org.koin.compose.viewmodel.koinViewModel

private sealed interface UsersNav {
    object List : UsersNav
    data class Detail(val user: User) : UsersNav
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(vm: UsersViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var nav: UsersNav by remember { mutableStateOf(UsersNav.List) }

    LaunchedEffect(state.actionError) {
        state.actionError?.let {
            snackbar.showSnackbar(it)
            vm.clearActionError()
        }
    }

    LaunchedEffect(state.users) {
        val current = nav as? UsersNav.Detail ?: return@LaunchedEffect
        val updated = state.users.find { it.uuid == current.user.uuid } ?: return@LaunchedEffect
        if (updated != current.user) nav = UsersNav.Detail(updated)
    }

    if (state.formVisible) {
        UserCreateEditScreen(state, vm)
        return
    }

    val isDetail = nav is UsersNav.Detail

    // SeekableTransitionState drives the slide animation and responds to predictive back.
    val transitionState = remember { SeekableTransitionState(false) }
    val transition = rememberTransition(transitionState, label = "users_nav")

    // Sync transition with nav state (forward navigation or "← Back" button press).
    LaunchedEffect(isDetail) { transitionState.animateTo(isDetail) }

    // Snapshot the last non-null user so the detail content stays available during the
    // exit animation even after nav has been reset to List.
    var lastDetailUser by remember { mutableStateOf<User?>(null) }
    val currentDetailUser = (nav as? UsersNav.Detail)?.user
    if (currentDetailUser != null) lastDetailUser = currentDetailUser

    // Predictive back: seek the transition during the drag, complete or reverse on release.
    BackGestureEffect(
        enabled = isDetail,
        onProgress = { fraction -> transitionState.seekTo(fraction, false) },
        onCommit   = { transitionState.animateTo(false); nav = UsersNav.List },
        onCancel   = { transitionState.animateTo(true) },
    )

    transition.AnimatedContent(
        contentKey = { it },
        transitionSpec = {
            if (targetState) {
                // Forward — detail slides in from the right, on top; list parallaxes left underneath.
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 4 }
            } else {
                // Back — detail (on top) slides out to the right, revealing the list underneath.
                slideInHorizontally { -it / 4 } togetherWith slideOutHorizontally { it }
            }.apply { targetContentZIndex = if (targetState) 1f else 0f }
        },
    ) { showDetail ->
        if (showDetail) {
            lastDetailUser?.let { user ->
                UserDetailScreen(
                    user          = user,
                    onBack        = { nav = UsersNav.List },
                    onEdit        = { vm.openEditForm(user) },
                    onEnable      = { vm.enableUser(user.uuid) },
                    onDisable     = { vm.disableUser(user.uuid) },
                    onResetTraffic= { vm.resetTraffic(user.uuid) },
                    onDelete      = { vm.deleteUser(user.uuid); nav = UsersNav.List },
                )
            }
        } else {
            Scaffold(
                contentWindowInsets = WindowInsets(0),
                snackbarHost = { SnackbarHost(snackbar) },
                topBar = {
                    FwTopBar(
                        title = "Users (${state.filtered.size})",
                        actions = { TextButton(onClick = vm::load) { Text("Refresh") } },
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick        = vm::openCreateForm,
                        shape          = MaterialTheme.shapes.large,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor   = MaterialTheme.colorScheme.onPrimary,
                        elevation      = FloatingActionButtonDefaults.elevation(0.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "New user", modifier = Modifier.size(28.dp))
                    }
                },
            ) { padding ->
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    OutlinedTextField(
                        value         = state.query,
                        onValueChange = vm::onQueryChange,
                        placeholder   = { Text("Search by username or tag") },
                        leadingIcon   = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine    = true,
                        shape         = MaterialTheme.shapes.medium,
                        modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    when {
                        state.isLoading && state.users.isEmpty() ->
                            ShimmerList()

                        state.error != null && state.users.isEmpty() ->
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                                    Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
                                }
                            }

                        else ->
                            PullToRefreshBox(
                                isRefreshing = state.isLoading && state.users.isNotEmpty(),
                                onRefresh    = vm::load,
                            ) {
                                if (state.filtered.isEmpty()) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            if (state.query.isBlank()) "No users" else "No results for \"${state.query}\"",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        items(state.filtered, key = { it.uuid }) { user ->
                                            UserListItem(
                                                user    = user,
                                                onClick = { nav = UsersNav.Detail(user) },
                                            )
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserListItem(user: User, onClick: () -> Unit) {
    val monoFont = LocalFwMonoFont.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(user.username, style = MaterialTheme.typography.titleSmall)
                    StatusBadge(user.status)
                }
                if (!user.tag.isNullOrBlank()) {
                    Text(
                        user.tag!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val usedStr  = formatBytes(user.usedTrafficBytes)
                val limitStr = if (user.trafficLimitBytes > 0) formatBytes(user.trafficLimitBytes) else "∞"
                Text(
                    "$usedStr / $limitStr",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (user.trafficLimitBytes > 0) {
                    val fwStatus = LocalFwStatus.current
                    val progress = (user.usedTrafficBytes.toFloat() / user.trafficLimitBytes).coerceIn(0f, 1f)
                    val barColor = when {
                        progress >= 0.9f -> fwStatus.warning
                        else             -> fwStatus.online
                    }
                    LinearProgressIndicator(
                        progress    = { progress },
                        modifier    = Modifier.fillMaxWidth().padding(top = 2.dp),
                        color       = barColor,
                        trackColor  = MaterialTheme.colorScheme.surfaceContainerHigh,
                        strokeCap   = StrokeCap.Round,
                    )
                }
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint   = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun StatusBadge(status: UserStatus) {
    val fwStatus = LocalFwStatus.current
    val (containerColor, labelColor) = when (status) {
        UserStatus.ACTIVE   -> Pair(fwStatus.online,   Color.Black)
        UserStatus.LIMITED  -> Pair(fwStatus.warning,  Color.Black)
        UserStatus.EXPIRED  -> Pair(fwStatus.offline,  Color.White)
        UserStatus.DISABLED -> Pair(fwStatus.neutral,  Color.Black)
    }
    Badge(
        containerColor = containerColor,
        contentColor   = labelColor,
        modifier = Modifier.padding(0.dp),
    ) {
        Text(
            status.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDetailScreen(
    user: User,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onResetTraffic: () -> Unit,
    onDelete: () -> Unit
) {
    val monoFont = LocalFwMonoFont.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete ${user.username}") },
            text  = { Text("Delete ${user.username}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { FwDetailTopBar(title = user.username, onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FwDetailCard {
                    DetailSectionTitle("Info")
                    DetailRow("Status", user.status.label, monoFont)
                    DetailRow("UUID", user.shortUuid, monoFont)
                    DetailRow("Strategy", user.trafficLimitStrategy, monoFont)
                    DetailRow("Expires", user.expireAt.take(10), monoFont)
                    user.email?.let { DetailRow("Email", it, monoFont) }
                    user.tag?.let { DetailRow("Tag", it, monoFont) }
                    user.description?.let { DetailRow("Notes", it, monoFont) }
                }
            }
            item {
                FwDetailCard {
                    DetailSectionTitle("Traffic")
                    DetailRow("Used", formatBytes(user.usedTrafficBytes), monoFont)
                    DetailRow("Limit", if (user.trafficLimitBytes > 0) formatBytes(user.trafficLimitBytes) else "Unlimited", monoFont)
                    DetailRow("Lifetime", formatBytes(user.lifetimeUsedTrafficBytes), monoFont)
                }
            }
            if (user.activeSquads.isNotEmpty()) {
                item {
                    FwDetailCard {
                        DetailSectionTitle("Squads")
                        user.activeSquads.forEach {
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(percent = 50),
                    ) { Text("Edit User") }
                    if (user.isActive) {
                        Button(
                            onClick = onDisable,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) { Text("Disable User") }
                    } else {
                        Button(
                            onClick = onEnable,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                        ) { Text("Enable User") }
                    }
                    Button(
                        onClick = onResetTraffic,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurface)
                    ) { Text("Reset Traffic") }
                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                    ) { Text("Delete User") }
                }
            }
        }
    }
}

@Composable
private fun FwDetailCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = { content() },
        )
    }
}

@Composable
private fun DetailSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall)
}

@Composable
private fun DetailRow(label: String, value: String, monoFont: androidx.compose.ui.text.font.FontFamily) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style    = MaterialTheme.typography.bodyMedium.copy(fontFamily = monoFont),
            fontWeight = FontWeight.Medium,
        )
    }
}
