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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import art.yniyniyni.freedomwave.ui.navigation.BackGestureEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import art.yniyniyni.freedomwave.ui.components.TrafficDonut
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import art.yniyniyni.freedomwave.domain.model.HwidDevice
import art.yniyniyni.freedomwave.domain.model.IpRow
import art.yniyniyni.freedomwave.domain.model.Node
import art.yniyniyni.freedomwave.domain.model.User
import art.yniyniyni.freedomwave.domain.model.UserStatus
import art.yniyniyni.freedomwave.ui.components.ShimmerList
import art.yniyniyni.freedomwave.ui.l10n.localized
import art.yniyniyni.freedomwave.ui.l10n.localizedBytes
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import art.yniyniyni.freedomwave.ui.theme.LocalFwStatus
import art.yniyniyni.freedomwave.util.countryFlag
import art.yniyniyni.freedomwave.util.expiryRemaining
import art.yniyniyni.freedomwave.util.parseInstant
import art.yniyniyni.freedomwave.util.relativePast
import androidx.compose.foundation.Image
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.datetime.Clock
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private sealed interface UsersNav {
    data object List : UsersNav
    data class Detail(val user: User) : UsersNav
    data class Form(val editing: User?) : UsersNav

    val depth: Int get() = when (this) {
        is List -> 0
        is Detail -> 1
        is Form -> 2
    }
    val key: String get() = when (this) {
        is List -> "list"
        is Detail -> "detail:${user.uuid}"
        is Form -> "form:${editing?.uuid ?: "new"}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(vm: UsersViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var stack by remember { mutableStateOf<kotlin.collections.List<UsersNav>>(listOf(UsersNav.List)) }
    val top = stack.last()
    val canGoBack = stack.size > 1

    val actionErrorText = state.actionError?.resolve()
    LaunchedEffect(actionErrorText) {
        actionErrorText?.let {
            snackbar.showSnackbar(it)
            vm.clearActionError()
        }
    }

    val transitionState = remember { SeekableTransitionState<UsersNav>(UsersNav.List) }
    val transition = rememberTransition(transitionState, label = "users_nav")

    // Animate to the current top whenever it changes via forward navigation or a programmatic pop.
    LaunchedEffect(top) {
        if (transitionState.currentState != top) transitionState.animateTo(top)
    }

    // Predictive back: seek toward the previous entry, commit pops, cancel returns to top.
    BackGestureEffect(
        enabled = canGoBack,
        onProgress = { fraction -> transitionState.seekTo(fraction, stack[stack.size - 2]) },
        onCommit   = {
            val target = stack[stack.size - 2]
            transitionState.animateTo(target)
            stack = stack.dropLast(1)
        },
        onCancel   = { transitionState.animateTo(top) },
    )

    transition.AnimatedContent(
        contentKey = { it.key },
        transitionSpec = {
            val deeper = targetState.depth > initialState.depth
            if (deeper) {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 4 }
            } else {
                slideInHorizontally { -it / 4 } togetherWith slideOutHorizontally { it }
            }.apply { targetContentZIndex = if (deeper) 1f else 0f }
        },
    ) { navEntry ->
        when (navEntry) {
            is UsersNav.List -> UsersListContent(
                state = state,
                vm = vm,
                snackbar = snackbar,
                onOpenCreate = { vm.openCreateForm(); stack = stack + UsersNav.Form(null) },
                onOpenDetail = { user -> stack = stack + UsersNav.Detail(user) },
            )
            is UsersNav.Detail -> {
                val live = state.users.find { it.uuid == navEntry.user.uuid } ?: navEntry.user
                UserDetailScreen(
                    user           = live,
                    nodesByUuid    = state.nodesByUuid,
                    onBack         = { stack = stack.dropLast(1) },
                    onEdit         = { vm.openEditForm(live); stack = stack + UsersNav.Form(live) },
                    onEnable       = { vm.enableUser(live.uuid) },
                    onDisable      = { vm.disableUser(live.uuid) },
                    onResetTraffic = { vm.resetTraffic(live.uuid) },
                    onDelete       = { vm.deleteUser(live.uuid); stack = stack.dropLast(1) },
                    onApplyUpdate  = { updated -> vm.applyUserUpdate(updated) },
                )
            }
            is UsersNav.Form -> {
                // Pop only when the Form is still on top. saveForm's onSuccess is async, so the
                // user may have already backed out before it fires — popping unconditionally
                // (twice) would empty the stack and crash stack.last(). Guarding makes both
                // onBack and onSaved idempotent.
                val dismissForm = {
                    if (stack.lastOrNull() is UsersNav.Form) stack = stack.dropLast(1)
                }
                UserCreateEditScreen(
                    state = state,
                    vm = vm,
                    onBack = dismissForm,
                    onSaved = dismissForm,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsersListContent(
    state: UsersUiState,
    vm: UsersViewModel,
    snackbar: SnackbarHostState,
    onOpenCreate: () -> Unit,
    onOpenDetail: (User) -> Unit,
) {
    var sortMenuOpen by remember { mutableStateOf(false) }
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            FwTopBar(
                title = "Users (${state.visible.size})",
                actions = {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(Icons.Rounded.SwapVert, contentDescription = "Sort")
                    }
                    DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                        UserSortField.entries.forEach { field ->
                            val active = state.sortField == field
                            DropdownMenuItem(
                                text = { Text(field.label) },
                                onClick = { vm.onSortSelected(field) },
                                trailingIcon = {
                                    if (active) Icon(
                                        if (state.sortAscending) Icons.Rounded.ArrowUpward
                                        else Icons.Rounded.ArrowDownward,
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                    }
                    TextButton(onClick = vm::load) { Text("Refresh") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onOpenCreate,
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
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            ) {
                items(UserCategory.entries) { cat ->
                    FilterChip(
                        selected = state.category == cat,
                        onClick  = { vm.onCategorySelected(cat) },
                        label    = { Text(cat.label) },
                    )
                }
            }
            when {
                state.isLoading && state.users.isEmpty() -> ShimmerList()
                state.error != null && state.users.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error!!.resolve(), color = MaterialTheme.colorScheme.error)
                            Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
                        }
                    }
                else ->
                    PullToRefreshBox(
                        isRefreshing = state.isLoading && state.users.isNotEmpty(),
                        onRefresh    = vm::load,
                    ) {
                        if (state.visible.isEmpty()) {
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
                                items(state.visible, key = { it.uuid }) { user ->
                                    UserListItem(
                                        user        = user,
                                        nodesByUuid = state.nodesByUuid,
                                        onClick     = { onOpenDetail(user) },
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun UserListItem(
    user: User,
    nodesByUuid: Map<String, Node>,
    onClick: () -> Unit,
) {
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
                val usedStr  = localizedBytes(user.usedTrafficBytes)
                val limitStr = if (user.trafficLimitBytes > 0) localizedBytes(user.trafficLimitBytes) else "∞"
                Text(
                    "$usedStr / $limitStr",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (user.trafficLimitBytes > 0) {
                    val fwStatus = LocalFwStatus.current
                    val progress = (user.usedTrafficBytes.toFloat() / user.trafficLimitBytes).coerceIn(0f, 1f)
                    val barColor = if (progress >= 0.9f) fwStatus.warning else fwStatus.online
                    LinearProgressIndicator(
                        progress    = { progress },
                        modifier    = Modifier.fillMaxWidth().padding(top = 2.dp),
                        color       = barColor,
                        trackColor  = MaterialTheme.colorScheme.surfaceContainerHigh,
                        strokeCap   = StrokeCap.Round,
                    )
                }
                // Last connection: 🇩🇪 DE · node · 5m ago
                val node = user.lastConnectedNodeUuid?.let { nodesByUuid[it] }
                val lastSeen = relativePast(user.onlineAt).localized()
                val lastConn = buildString {
                    if (node != null && node.countryCode.isNotBlank()) {
                        append("${node.countryCode} ${countryFlag(node.countryCode)} · ")
                    }
                    if (node != null) append("${node.name} · ")
                    append(lastSeen)
                }
                Text(
                    if (user.onlineAt == null) "Never connected" else lastConn,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Expiry remaining
                Text(
                    expiryRemaining(user.expireAt).localized(),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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

// ─────────────────────────────────────────────────────────────────────────────
// UserDetailScreen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDetailScreen(
    user: User,
    nodesByUuid: Map<String, Node>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onResetTraffic: () -> Unit,
    onDelete: () -> Unit,
    onApplyUpdate: (User) -> Unit,
) {
    val detailVm: UserDetailViewModel = koinViewModel(key = user.uuid) { parametersOf(user.uuid) }
    val detailState by detailVm.state.collectAsState()

    val monoFont = LocalFwMonoFont.current
    val snackbar = remember { SnackbarHostState() }

    // Feedback snackbars
    val detailActionErrorText = detailState.actionError?.resolve()
    LaunchedEffect(detailActionErrorText) {
        detailActionErrorText?.let { snackbar.showSnackbar(it); detailVm.clearMessages() }
    }
    val detailActionSuccessText = detailState.actionSuccess?.resolve()
    LaunchedEffect(detailActionSuccessText) {
        detailActionSuccessText?.let { snackbar.showSnackbar(it); detailVm.clearMessages() }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────────
    var showDeleteConfirm  by remember { mutableStateOf(false) }
    var showQrDialog       by remember { mutableStateOf(false) }
    var showCopiedSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(showCopiedSnackbar) {
        if (showCopiedSnackbar) { snackbar.showSnackbar("Copied"); showCopiedSnackbar = false }
    }

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

    if (showQrDialog) {
        QrDialog(url = user.subscriptionUrl, onDismiss = { showQrDialog = false })
    }

    if (detailState.showSetLimitDialog) {
        SetLimitDialog(
            input     = detailState.setLimitGbInput,
            onInput   = detailVm::onSetLimitInput,
            onConfirm = { detailVm.confirmSetLimit(onApplyUpdate) },
            onDismiss = detailVm::dismissSetLimitDialog,
        )
    }

    if (detailState.showSetExpiryDialog) {
        SetExpiryDialog(
            millis    = detailState.setExpiryMillis,
            onChange  = detailVm::onSetExpiryMillis,
            onConfirm = { detailVm.confirmSetExpiry(onApplyUpdate) },
            onDismiss = detailVm::dismissSetExpiryDialog,
        )
    }

    if (detailState.showDeviceLimitDialog) {
        DeviceLimitDialog(
            input     = detailState.deviceLimitInput,
            onInput   = detailVm::onDeviceLimitInput,
            onConfirm = { detailVm.confirmDeviceLimit(onApplyUpdate) },
            onDismiss = detailVm::dismissDeviceLimitDialog,
        )
    }

    val clipboard = LocalClipboardManager.current

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            FwDetailTopBar(
                title = user.username,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showQrDialog = true }) {
                        Icon(Icons.Rounded.QrCode, contentDescription = "QR Code")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Info card ──────────────────────────────────────────────────────
            item {
                FwDetailCard {
                    // Compact header: title + status badge on one line.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DetailSectionTitle("Info")
                        StatusBadge(user.status)
                    }
                    // Last connection, formatted like the user list: 🇩🇪 DE · node · 5m ago
                    val node = user.lastConnectedNodeUuid?.let { nodesByUuid[it] }
                    val lastSeen = relativePast(user.onlineAt).localized()
                    val lastConn = buildString {
                        if (node != null && node.countryCode.isNotBlank()) {
                            append("${node.countryCode} ${countryFlag(node.countryCode)} · ")
                        }
                        if (node != null) append("${node.name} · ")
                        append(lastSeen)
                    }
                    DetailRow("Last seen", if (user.onlineAt == null) "Never connected" else lastConn, monoFont)
                    DetailRow("Expires",  expiryRemaining(user.expireAt).localized(), monoFont)
                    user.email?.let { DetailRow("Email", it, monoFont) }
                    user.tag?.let   { DetailRow("Tag",   it, monoFont) }
                    user.description?.let { DetailRow("Notes", it, monoFont) }
                }
            }

            // ── Traffic donut ─────────────────────────────────────────────────
            item {
                FwDetailCard {
                    DetailSectionTitle("Traffic")
                    Spacer(Modifier.height(8.dp))
                    TrafficDonut(
                        usedBytes     = user.usedTrafficBytes,
                        limitBytes    = user.trafficLimitBytes,
                        lifetimeBytes = user.lifetimeUsedTrafficBytes,
                    )
                    // Reset strategy only matters when traffic actually resets.
                    if (user.trafficLimitStrategy.isNotBlank() && user.trafficLimitStrategy != "NO_RESET") {
                        Spacer(Modifier.height(8.dp))
                        DetailRow(
                            "Resets",
                            user.trafficLimitStrategy.lowercase().replace('_', ' ')
                                .replaceFirstChar { it.uppercase() },
                            monoFont,
                        )
                    }
                }
            }

            // ── Subscription URL ──────────────────────────────────────────────
            if (user.subscriptionUrl.isNotBlank()) {
                item {
                    FwDetailCard {
                        DetailSectionTitle("Subscription")
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboard.setText(AnnotatedString(user.subscriptionUrl))
                                    showCopiedSnackbar = true
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text     = user.subscriptionUrl,
                                style    = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            // ── Squads ─────────────────────────────────────────────────────────
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

            // ── Devices section ───────────────────────────────────────────────
            item {
                FwDetailCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { detailVm.toggleDevicesExpanded() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            DetailSectionTitle("Devices")
                            if (!detailState.devicesLoading) {
                                Badge { Text("${detailState.devices.size}") }
                            }
                        }
                        Icon(
                            if (detailState.devicesExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (detailState.devicesExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (detailState.devicesExpanded) {
                        Spacer(Modifier.height(4.dp))
                        when {
                            detailState.devicesLoading ->
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                    )
                                }
                            detailState.devicesError != null ->
                                Text(
                                    detailState.devicesError!!.resolve(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            detailState.devices.isEmpty() ->
                                Text(
                                    "No registered devices",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            else -> detailState.devices.forEach { device ->
                                DeviceRow(device = device, monoFont = monoFont)
                            }
                        }
                    }
                }
            }

            // ── IP Addresses section ─────────────────────────────────────────
            item {
                FwDetailCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { detailVm.toggleIpExpanded() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            DetailSectionTitle("IP Addresses")
                            if (!detailState.ipLoading) {
                                Badge { Text("${detailState.ipRows.size} unique") }
                            }
                        }
                        Icon(
                            if (detailState.ipExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (detailState.ipExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (detailState.ipExpanded) {
                        Spacer(Modifier.height(4.dp))
                        when {
                            detailState.ipLoading ->
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                    )
                                }
                            detailState.ipError != null ->
                                Text(
                                    detailState.ipError!!.resolve(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            detailState.ipRows.isEmpty() ->
                                Text(
                                    "No subscription requests recorded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            else -> detailState.ipRows.forEach { row ->
                                IpAddressRow(row = row, monoFont = monoFont)
                            }
                        }
                    }
                }
            }

            // ── Manage card ───────────────────────────────────────────────────
            item {
                ManageCard(
                    user           = user,
                    detailVm       = detailVm,
                    onEdit         = onEdit,
                    onEnable       = onEnable,
                    onDisable      = onDisable,
                    onResetTraffic = onResetTraffic,
                    onDelete       = { showDeleteConfirm = true },
                    onApplyUpdate  = onApplyUpdate,
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Manage card — Status, Traffic & Expiration, Device limit, Danger zone
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ManageCard(
    user: User,
    detailVm: UserDetailViewModel,
    onEdit: () -> Unit,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onResetTraffic: () -> Unit,
    onDelete: () -> Unit,
    onApplyUpdate: (User) -> Unit,
) {
    FwDetailCard {
        DetailSectionTitle("Manage")
        Spacer(Modifier.height(4.dp))

        // Status row
        ManageSectionLabel("Status")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (user.isActive) {
                Button(
                    onClick = onDisable,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) { Text("Disable") }
            } else {
                Button(
                    onClick = onEnable,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(percent = 50),
                ) { Text("Enable") }
            }
            OutlinedButton(
                onClick = { detailVm.revokeSubscription(onApplyUpdate) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
            ) { Text("Revoke Sub") }
        }

        Spacer(Modifier.height(4.dp))

        // Traffic & Expiration
        ManageSectionLabel("Traffic & Expiration")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onResetTraffic,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
            ) { Text("Reset Traffic") }
            OutlinedButton(
                onClick = {
                    detailVm.openSetLimitDialog(user.trafficLimitBytes)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
            ) { Text("Set Limit") }
            OutlinedButton(
                onClick = {
                    val millis = parseInstant(user.expireAt)?.toEpochMilliseconds()
                        ?: Clock.System.now().toEpochMilliseconds()
                    detailVm.openSetExpiryDialog(millis)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
            ) { Text("Set Expiry") }
        }

        Spacer(Modifier.height(4.dp))

        // Device limit stepper
        ManageSectionLabel("Device Limit")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val limitLabel = user.hwidDeviceLimit?.toString() ?: "Fallback"
            Text(
                limitLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = LocalFwMonoFont.current),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = { detailVm.openDeviceLimitDialog(user.hwidDeviceLimit) },
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) { Text("Edit") }
            OutlinedButton(
                onClick = { detailVm.adjustDeviceLimit(user.hwidDeviceLimit, -1, onApplyUpdate) },
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
            ) { Text("−") }
            OutlinedButton(
                onClick = { detailVm.adjustDeviceLimit(user.hwidDeviceLimit, +1, onApplyUpdate) },
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
            ) { Text("+") }
        }

        Spacer(Modifier.height(4.dp))

        // Edit User (full form)
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(percent = 50),
        ) { Text("Edit User") }

        Spacer(Modifier.height(4.dp))

        // Danger zone
        ManageSectionLabel("Danger Zone")
        Button(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor   = MaterialTheme.colorScheme.onError,
            ),
        ) { Text("Delete User") }
    }
}

@Composable
private fun ManageSectionLabel(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Device row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeviceRow(device: HwidDevice, monoFont: FontFamily) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // Device model or HWID
            Text(
                device.deviceModel ?: device.hwid.take(16),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            // Platform + OS version
            val platformStr = buildString {
                device.platform?.let { append(it) }
                device.osVersion?.let {
                    if (isNotEmpty()) append(" · ")
                    append(it)
                }
            }
            if (platformStr.isNotBlank()) {
                Text(
                    platformStr,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // User agent (truncated)
            device.userAgent?.let { ua ->
                Text(
                    ua,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            relativePast(device.updatedAt).localized(),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// IP row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IpAddressRow(row: IpRow, monoFont: FontFamily) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Flag + country code
                row.countryCode?.takeIf { it.length == 2 }?.let { cc ->
                    Text(
                        "${countryFlag(cc)} $cc",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    row.ip,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = monoFont),
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    "${row.count}×",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // Geo line: city · region · ISP
            val geoLine = buildString {
                row.city?.let { append(it) }
                row.region?.let { if (isNotEmpty()) append(" · "); append(it) }
                row.isp?.let { if (isNotEmpty()) append(" · "); append(it) }
            }
            if (geoLine.isNotBlank()) {
                Text(
                    geoLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            relativePast(row.lastSeenAt).localized(),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QR dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QrDialog(url: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Subscription QR", style = MaterialTheme.typography.titleMedium)
                val painter = rememberQrCodePainter(url)
                // qrose draws dark cells on a transparent background, so on a dark-theme dialog the
                // code would vanish. Render it on a fixed white quiet zone — scannable in any theme.
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Image(
                        painter     = painter,
                        contentDescription = "QR code",
                        modifier    = Modifier.size(220.dp),
                    )
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick-edit dialogs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SetLimitDialog(
    input: String,
    onInput: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text("Set Traffic Limit") },
        text   = {
            OutlinedTextField(
                value         = input,
                onValueChange = onInput,
                label         = { Text("Limit (GB)") },
                supportingText = { Text("0 = unlimited") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Set") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun DeviceLimitDialog(
    input: String,
    onInput: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text("Set Device Limit") },
        text   = {
            OutlinedTextField(
                value         = input,
                onValueChange = onInput,
                label         = { Text("Devices") },
                supportingText = { Text("0 = unlimited") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Set") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetExpiryDialog(
    millis: Long,
    onChange: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = MaterialTheme.shapes.extraLarge) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Set Expiry", style = MaterialTheme.typography.titleMedium)
                ExpiryEditor(expireMillis = millis, enabled = true, onChange = onChange)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss)  { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) { Text("Set") }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FwDetailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun DetailSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall)
}

@Composable
private fun DetailRow(label: String, value: String, monoFont: FontFamily) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style      = MaterialTheme.typography.bodyMedium.copy(fontFamily = monoFont),
            fontWeight = FontWeight.Medium,
        )
    }
}
