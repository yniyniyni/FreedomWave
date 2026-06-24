package art.yniyniyni.freedomwave.ui.feature.users

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import art.yniyniyni.freedomwave.ui.components.FwNavDestination
import art.yniyniyni.freedomwave.ui.components.FwNavigationContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import art.yniyniyni.freedomwave.ui.components.WaveLoader
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
import art.yniyniyni.freedomwave.ui.components.DetailRow
import art.yniyniyni.freedomwave.ui.components.DetailSectionTitle
import art.yniyniyni.freedomwave.ui.components.FwDetailCard
import art.yniyniyni.freedomwave.ui.components.FwDetailTopBar
import art.yniyniyni.freedomwave.ui.components.FwSectionIcon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import art.yniyniyni.freedomwave.domain.model.HwidDevice
import art.yniyniyni.freedomwave.domain.model.IpRow
import art.yniyniyni.freedomwave.domain.model.Node
import art.yniyniyni.freedomwave.domain.model.User
import art.yniyniyni.freedomwave.domain.model.UserStatus
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_cancel
import freedomwave.composeapp.generated.resources.common_close
import freedomwave.composeapp.generated.resources.common_copied
import freedomwave.composeapp.generated.resources.common_minus_sign
import freedomwave.composeapp.generated.resources.common_plus_sign
import freedomwave.composeapp.generated.resources.common_copy
import freedomwave.composeapp.generated.resources.common_delete
import freedomwave.composeapp.generated.resources.common_retry
import freedomwave.composeapp.generated.resources.symbol_infinity
import freedomwave.composeapp.generated.resources.users_collapse
import freedomwave.composeapp.generated.resources.users_danger_zone
import freedomwave.composeapp.generated.resources.users_delete_confirm
import freedomwave.composeapp.generated.resources.users_delete_title
import freedomwave.composeapp.generated.resources.users_delete_user
import freedomwave.composeapp.generated.resources.users_detail_devices
import freedomwave.composeapp.generated.resources.users_detail_email
import freedomwave.composeapp.generated.resources.users_detail_expires
import freedomwave.composeapp.generated.resources.users_detail_info
import freedomwave.composeapp.generated.resources.users_detail_ip_addresses
import freedomwave.composeapp.generated.resources.users_detail_last_seen
import freedomwave.composeapp.generated.resources.users_detail_notes
import freedomwave.composeapp.generated.resources.users_detail_resets
import freedomwave.composeapp.generated.resources.users_detail_squads
import freedomwave.composeapp.generated.resources.users_detail_subscription
import freedomwave.composeapp.generated.resources.users_detail_tag
import freedomwave.composeapp.generated.resources.users_detail_traffic
import freedomwave.composeapp.generated.resources.users_device_limit
import freedomwave.composeapp.generated.resources.users_device_limit_fallback
import freedomwave.composeapp.generated.resources.users_disable
import freedomwave.composeapp.generated.resources.users_edit
import freedomwave.composeapp.generated.resources.users_edit_user
import freedomwave.composeapp.generated.resources.users_empty
import freedomwave.composeapp.generated.resources.users_enable
import freedomwave.composeapp.generated.resources.users_expand
import freedomwave.composeapp.generated.resources.users_ip_unique
import freedomwave.composeapp.generated.resources.users_limit_gb
import freedomwave.composeapp.generated.resources.users_manage
import freedomwave.composeapp.generated.resources.users_manage_status
import freedomwave.composeapp.generated.resources.users_manage_traffic_expiry
import freedomwave.composeapp.generated.resources.users_never_connected
import freedomwave.composeapp.generated.resources.users_new_user
import freedomwave.composeapp.generated.resources.users_no_devices
import freedomwave.composeapp.generated.resources.users_no_results
import freedomwave.composeapp.generated.resources.users_no_sub_requests
import freedomwave.composeapp.generated.resources.users_qr_code
import freedomwave.composeapp.generated.resources.common_refresh
import freedomwave.composeapp.generated.resources.users_reset_traffic
import freedomwave.composeapp.generated.resources.users_revoke_sub
import freedomwave.composeapp.generated.resources.users_search_placeholder
import freedomwave.composeapp.generated.resources.users_set
import freedomwave.composeapp.generated.resources.users_set_device_limit_title
import freedomwave.composeapp.generated.resources.users_set_expiry
import freedomwave.composeapp.generated.resources.users_set_limit
import freedomwave.composeapp.generated.resources.users_set_traffic_limit_title
import freedomwave.composeapp.generated.resources.users_sort
import freedomwave.composeapp.generated.resources.users_subscription_qr
import freedomwave.composeapp.generated.resources.users_title_count
import freedomwave.composeapp.generated.resources.users_traffic_view_stats
import freedomwave.composeapp.generated.resources.users_zero_unlimited
import art.yniyniyni.freedomwave.ui.l10n.localized
import art.yniyniyni.freedomwave.ui.l10n.localizedBytes
import art.yniyniyni.freedomwave.ui.l10n.resolve
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
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

private sealed interface UsersNav : FwNavDestination {
    data object List : UsersNav
    data class Detail(val user: User) : UsersNav
    data class Form(val editing: User?) : UsersNav
    data class TrafficStats(val user: User) : UsersNav

    override val depth: Int get() = when (this) {
        is List -> 0
        is Detail -> 1
        is Form -> 2
        is TrafficStats -> 2
    }
    override val key: String get() = when (this) {
        is List -> "list"
        is Detail -> "detail:${user.uuid}"
        is Form -> "form:${editing?.uuid ?: "new"}"
        is TrafficStats -> "traffic_stats:${user.uuid}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(vm: UsersViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()

    FwNavigationContainer<UsersNav>(
        navLabel = "users_nav",
        rootState = UsersNav.List,
        initialStack = listOf(UsersNav.List),
        actionError = state.actionError,
        onClearActionError = vm::clearActionError,
        contentKey = { it.key },
    ) { navEntry, push, pop, currentStack, snackbarHost ->
        when (navEntry) {
            is UsersNav.List -> UsersListContent(
                state = state,
                vm = vm,
                snackbar = snackbarHost,
                onOpenCreate = { vm.openCreateForm(); push(UsersNav.Form(null)) },
                onOpenDetail = { user -> push(UsersNav.Detail(user)) },
            )
            is UsersNav.Detail -> {
                val live = state.users.find { it.uuid == navEntry.user.uuid } ?: navEntry.user
                UserDetailScreen(
                    user           = live,
                    nodesByUuid    = state.nodesByUuid,
                    onBack         = { pop() },
                    onEdit         = { vm.openEditForm(live); push(UsersNav.Form(live)) },
                    onEnable       = { vm.enableUser(live.uuid) },
                    onDisable      = { vm.disableUser(live.uuid) },
                    onResetTraffic = { vm.resetTraffic(live.uuid) },
                    onDelete       = { vm.deleteUser(live.uuid); pop() },
                    onApplyUpdate  = { updated -> vm.applyUserUpdate(updated) },
                    onTrafficStats = { push(UsersNav.TrafficStats(live)) },
                )
            }
            is UsersNav.Form -> {
                // Pop only when the Form is still on top. saveForm's onSuccess is async, so the
                // user may have already backed out before it fires — popping unconditionally
                // (twice) would empty the stack and crash stack.last(). Guarding makes both
                // onBack and onSaved idempotent.
                val dismissForm = {
                    if (currentStack().lastOrNull() is UsersNav.Form) pop()
                }
                UserCreateEditScreen(
                    state = state,
                    vm = vm,
                    onBack = dismissForm,
                    onSaved = dismissForm,
                )
            }
            is UsersNav.TrafficStats -> {
                UserTrafficStatsScreen(
                    user = navEntry.user,
                    viewModel = koinViewModel(key = navEntry.user.uuid) { parametersOf(navEntry.user.uuid) },
                    onBackClick = { pop() }
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
                title = stringResource(Res.string.users_title_count, state.visible.size),
                actions = {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(Icons.Rounded.SwapVert, contentDescription = stringResource(Res.string.users_sort))
                    }
                    DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                        val sortEntries = remember { UserSortField.entries }
                        sortEntries.forEach { field ->
                            val active = state.sortField == field
                            DropdownMenuItem(
                                text = { Text(field.label()) },
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
                    IconButton(onClick = vm::load) { Icon(Icons.Rounded.Refresh, contentDescription = stringResource(Res.string.common_refresh)) }
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
                Icon(Icons.Rounded.Add, contentDescription = stringResource(Res.string.users_new_user), modifier = Modifier.size(28.dp))
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value         = state.query,
                onValueChange = vm::onQueryChange,
                placeholder   = { Text(stringResource(Res.string.users_search_placeholder)) },
                leadingIcon   = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine    = true,
                shape         = MaterialTheme.shapes.medium,
                modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            val categoryEntries = remember { UserCategory.entries }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            ) {
                items(categoryEntries) { cat ->
                    FilterChip(
                        selected = state.category == cat,
                        onClick  = { vm.onCategorySelected(cat) },
                        label    = { Text(cat.label()) },
                    )
                }
            }
            when {
                state.isLoading && state.users.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { WaveLoader() }
                state.error != null && state.users.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error!!.resolve(), color = MaterialTheme.colorScheme.error)
                            Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) { Text(stringResource(Res.string.common_retry)) }
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
                                    if (state.query.isBlank()) stringResource(Res.string.users_empty)
                                    else stringResource(Res.string.users_no_results, state.query),
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
                val limitStr = if (user.trafficLimitBytes > 0) localizedBytes(user.trafficLimitBytes)
                    else stringResource(Res.string.symbol_infinity)
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
                    if (user.onlineAt == null) stringResource(Res.string.users_never_connected) else lastConn,
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
            status.localized().uppercase(),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

// UserDetailScreen

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
    onTrafficStats: () -> Unit,
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

    // Dialogs
    var showDeleteConfirm  by remember { mutableStateOf(false) }
    var showQrDialog       by remember { mutableStateOf(false) }
    var showCopiedSnackbar by remember { mutableStateOf(false) }

    val copiedText = stringResource(Res.string.common_copied)
    LaunchedEffect(showCopiedSnackbar) {
        if (showCopiedSnackbar) { snackbar.showSnackbar(copiedText); showCopiedSnackbar = false }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(Res.string.users_delete_title, user.username)) },
            text  = { Text(stringResource(Res.string.users_delete_confirm, user.username)) },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(Res.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(Res.string.common_cancel)) }
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
                        Icon(Icons.Rounded.QrCode, contentDescription = stringResource(Res.string.users_qr_code))
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

            // Info card
            item {
                FwDetailCard {
                    // Compact header: title + status badge on one line.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DetailSectionTitle(stringResource(Res.string.users_detail_info), Icons.Rounded.Info)
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
                    DetailRow(
                        stringResource(Res.string.users_detail_last_seen),
                        if (user.onlineAt == null) stringResource(Res.string.users_never_connected) else lastConn,
                        monoFont,
                    )
                    DetailRow(stringResource(Res.string.users_detail_expires), expiryRemaining(user.expireAt).localized(), monoFont)
                    user.email?.let { DetailRow(stringResource(Res.string.users_detail_email), it, monoFont) }
                    user.tag?.let   { DetailRow(stringResource(Res.string.users_detail_tag),   it, monoFont) }
                    user.description?.let { DetailRow(stringResource(Res.string.users_detail_notes), it, monoFont) }
                }
            }

            // Traffic donut
            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.users_detail_traffic), Icons.Rounded.DataUsage, MaterialTheme.colorScheme.tertiary)
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
                            stringResource(Res.string.users_detail_resets),
                            trafficStrategyLabel(user.trafficLimitStrategy),
                            monoFont,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onTrafficStats,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.users_traffic_view_stats))
                    }
                }
            }

            // Subscription URL
            if (user.subscriptionUrl.isNotBlank()) {
                item {
                    FwDetailCard {
                        DetailSectionTitle(stringResource(Res.string.users_detail_subscription), Icons.Rounded.Link, MaterialTheme.colorScheme.secondary)
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
                                contentDescription = stringResource(Res.string.common_copy),
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            // Squads
            if (user.activeSquads.isNotEmpty()) {
                item {
                    FwDetailCard {
                        DetailSectionTitle(stringResource(Res.string.users_detail_squads), Icons.Rounded.Groups, MaterialTheme.colorScheme.primary)
                        user.activeSquads.forEach {
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Devices section
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
                            DetailSectionTitle(stringResource(Res.string.users_detail_devices), Icons.Rounded.Devices, MaterialTheme.colorScheme.tertiary)
                            if (!detailState.devicesLoading) {
                                Badge { Text("${detailState.devices.size}") }
                            }
                        }
                        val devicesRotation by animateFloatAsState(
                            if (detailState.devicesExpanded) 180f else 0f
                        )
                        Icon(
                            Icons.Rounded.ExpandMore,
                            contentDescription = stringResource(
                                if (detailState.devicesExpanded) Res.string.users_collapse else Res.string.users_expand
                            ),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(devicesRotation),
                        )
                    }

                    if (detailState.devicesExpanded) {
                        Spacer(Modifier.height(4.dp))
                        when {
                            detailState.devicesLoading ->
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    WaveLoader(modifier = Modifier.size(width = 36.dp, height = 24.dp))
                                }
                            detailState.devicesError != null ->
                                Text(
                                    detailState.devicesError!!.resolve(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            detailState.devices.isEmpty() ->
                                Text(
                                    stringResource(Res.string.users_no_devices),
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

            // IP Addresses section
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
                            DetailSectionTitle(stringResource(Res.string.users_detail_ip_addresses), Icons.Rounded.Public, MaterialTheme.colorScheme.secondary)
                            if (!detailState.ipLoading) {
                                Badge {
                                    Text(
                                        pluralStringResource(
                                            Res.plurals.users_ip_unique,
                                            detailState.ipRows.size,
                                            detailState.ipRows.size,
                                        )
                                    )
                                }
                            }
                        }
                        val ipRotation by animateFloatAsState(
                            if (detailState.ipExpanded) 180f else 0f
                        )
                        Icon(
                            Icons.Rounded.ExpandMore,
                            contentDescription = stringResource(
                                if (detailState.ipExpanded) Res.string.users_collapse else Res.string.users_expand
                            ),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(ipRotation),
                        )
                    }

                    if (detailState.ipExpanded) {
                        Spacer(Modifier.height(4.dp))
                        when {
                            detailState.ipLoading ->
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    WaveLoader(modifier = Modifier.size(width = 36.dp, height = 24.dp))
                                }
                            detailState.ipError != null ->
                                Text(
                                    detailState.ipError!!.resolve(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            detailState.ipRows.isEmpty() ->
                                Text(
                                    stringResource(Res.string.users_no_sub_requests),
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

            // Manage card
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

// Manage card

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
        DetailSectionTitle(stringResource(Res.string.users_manage), Icons.Rounded.Tune)
        Spacer(Modifier.height(4.dp))

        // Status row
        ManageSectionLabel(stringResource(Res.string.users_manage_status))
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
                ) { Text(stringResource(Res.string.users_disable)) }
            } else {
                Button(
                    onClick = onEnable,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(percent = 50),
                ) { Text(stringResource(Res.string.users_enable)) }
            }
            OutlinedButton(
                onClick = { detailVm.revokeSubscription(onApplyUpdate) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
            ) { Text(stringResource(Res.string.users_revoke_sub)) }
        }

        Spacer(Modifier.height(4.dp))

        // Traffic & Expiration
        ManageSectionLabel(stringResource(Res.string.users_manage_traffic_expiry))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onResetTraffic,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            ) { Text(stringResource(Res.string.users_reset_traffic), textAlign = TextAlign.Center, maxLines = 2) }
            OutlinedButton(
                onClick = {
                    detailVm.openSetLimitDialog(user.trafficLimitBytes)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            ) { Text(stringResource(Res.string.users_set_limit), textAlign = TextAlign.Center, maxLines = 2) }
            OutlinedButton(
                onClick = {
                    val millis = parseInstant(user.expireAt)?.toEpochMilliseconds()
                        ?: Clock.System.now().toEpochMilliseconds()
                    detailVm.openSetExpiryDialog(millis)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            ) { Text(stringResource(Res.string.users_set_expiry), textAlign = TextAlign.Center, maxLines = 2) }
        }

        Spacer(Modifier.height(4.dp))

        // Device limit stepper
        ManageSectionLabel(stringResource(Res.string.users_device_limit))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val limitLabel = user.hwidDeviceLimit?.toString()
                ?: stringResource(Res.string.users_device_limit_fallback)
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
            ) { Text(stringResource(Res.string.users_edit)) }
            OutlinedButton(
                onClick = { detailVm.adjustDeviceLimit(user.hwidDeviceLimit, -1, onApplyUpdate) },
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
            ) { Text(stringResource(Res.string.common_minus_sign)) }
            OutlinedButton(
                onClick = { detailVm.adjustDeviceLimit(user.hwidDeviceLimit, +1, onApplyUpdate) },
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
            ) { Text(stringResource(Res.string.common_plus_sign)) }
        }

        Spacer(Modifier.height(4.dp))

        // Edit User (full form)
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(percent = 50),
        ) { Text(stringResource(Res.string.users_edit_user)) }

        Spacer(Modifier.height(4.dp))

        // Danger zone
        ManageSectionLabel(stringResource(Res.string.users_danger_zone))
        Button(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor   = MaterialTheme.colorScheme.onError,
            ),
        ) { Text(stringResource(Res.string.users_delete_user)) }
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

// Device row

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

// IP row

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

// QR dialog

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
                Text(stringResource(Res.string.users_subscription_qr), style = MaterialTheme.typography.titleMedium)
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
                        contentDescription = stringResource(Res.string.users_qr_code),
                        modifier    = Modifier.size(220.dp),
                    )
                }
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_close)) }
            }
        }
    }
}

// Quick-edit dialogs

@Composable
private fun SetLimitDialog(
    input: String,
    onInput: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text(stringResource(Res.string.users_set_traffic_limit_title)) },
        text   = {
            OutlinedTextField(
                value         = input,
                onValueChange = onInput,
                shape         = MaterialTheme.shapes.medium,
                label         = { Text(stringResource(Res.string.users_limit_gb)) },
                supportingText = { Text(stringResource(Res.string.users_zero_unlimited)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(Res.string.users_set)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
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
        title  = { Text(stringResource(Res.string.users_set_device_limit_title)) },
        text   = {
            OutlinedTextField(
                value         = input,
                onValueChange = onInput,
                shape         = MaterialTheme.shapes.medium,
                label         = { Text(stringResource(Res.string.users_detail_devices)) },
                supportingText = { Text(stringResource(Res.string.users_zero_unlimited)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(Res.string.users_set)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
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
                Text(stringResource(Res.string.users_set_expiry), style = MaterialTheme.typography.titleMedium)
                ExpiryEditor(expireMillis = millis, enabled = true, onChange = onChange)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss)  { Text(stringResource(Res.string.common_cancel)) }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) { Text(stringResource(Res.string.users_set)) }
                }
            }
        }
    }
}

