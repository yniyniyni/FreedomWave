package art.yniyniyni.freedomwave.ui.feature.infrabilling

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import art.yniyniyni.freedomwave.ui.components.WaveLoader
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import art.yniyniyni.freedomwave.domain.model.BillRecord
import art.yniyniyni.freedomwave.domain.model.BillingNode
import art.yniyniyni.freedomwave.domain.model.BillingStats
import art.yniyniyni.freedomwave.domain.model.InfraProvider
import art.yniyniyni.freedomwave.ui.components.FwDetailTopBar
import art.yniyniyni.freedomwave.ui.components.FwTopBar
import art.yniyniyni.freedomwave.ui.l10n.localized
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import art.yniyniyni.freedomwave.ui.theme.LocalFwStatus
import art.yniyniyni.freedomwave.util.DurationUnit
import art.yniyniyni.freedomwave.util.ExpiryRemaining
import art.yniyniyni.freedomwave.util.countryFlag
import art.yniyniyni.freedomwave.util.expiryRemaining
import art.yniyniyni.freedomwave.util.format2
import art.yniyniyni.freedomwave.util.formatDate
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_cancel
import freedomwave.composeapp.generated.resources.common_create
import freedomwave.composeapp.generated.resources.common_delete
import freedomwave.composeapp.generated.resources.common_ok
import freedomwave.composeapp.generated.resources.common_refresh
import freedomwave.composeapp.generated.resources.common_retry
import freedomwave.composeapp.generated.resources.common_save
import freedomwave.composeapp.generated.resources.infra_add_node
import freedomwave.composeapp.generated.resources.infra_add_payment
import freedomwave.composeapp.generated.resources.infra_add_provider
import freedomwave.composeapp.generated.resources.infra_amount
import freedomwave.composeapp.generated.resources.infra_billing_date
import freedomwave.composeapp.generated.resources.infra_delete_confirm
import freedomwave.composeapp.generated.resources.infra_delete_title
import freedomwave.composeapp.generated.resources.infra_edit_provider
import freedomwave.composeapp.generated.resources.infra_empty_history
import freedomwave.composeapp.generated.resources.infra_empty_nodes
import freedomwave.composeapp.generated.resources.infra_empty_providers
import freedomwave.composeapp.generated.resources.infra_favicon_link
import freedomwave.composeapp.generated.resources.infra_login_url
import freedomwave.composeapp.generated.resources.infra_next_billing
import freedomwave.composeapp.generated.resources.infra_no_available_nodes
import freedomwave.composeapp.generated.resources.infra_node
import freedomwave.composeapp.generated.resources.infra_payment_date
import freedomwave.composeapp.generated.resources.infra_preset_month
import freedomwave.composeapp.generated.resources.infra_preset_today
import freedomwave.composeapp.generated.resources.infra_preset_tomorrow
import freedomwave.composeapp.generated.resources.infra_provider
import freedomwave.composeapp.generated.resources.infra_provider_bills
import freedomwave.composeapp.generated.resources.infra_provider_name
import freedomwave.composeapp.generated.resources.infra_provider_nodes
import freedomwave.composeapp.generated.resources.infra_stat_nodes
import freedomwave.composeapp.generated.resources.infra_stat_this_month
import freedomwave.composeapp.generated.resources.infra_stat_total_spent
import freedomwave.composeapp.generated.resources.infra_stat_upcoming
import freedomwave.composeapp.generated.resources.infra_tab_history
import freedomwave.composeapp.generated.resources.infra_tab_nodes
import freedomwave.composeapp.generated.resources.infra_tab_providers
import freedomwave.composeapp.generated.resources.infra_title
import freedomwave.composeapp.generated.resources.infra_update_billing_date
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.Clock
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfraBillingScreen(vm: InfraBillingViewModel = koinViewModel(), onBack: (() -> Unit)? = null) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    val actionErrorText = state.actionError?.resolve()
    LaunchedEffect(actionErrorText) {
        actionErrorText?.let { snackbar.showSnackbar(it); vm.clearActionError() }
    }

    // Dialogs
    when (state.dialog) {
        BillingDialog.ADD_PROVIDER, BillingDialog.EDIT_PROVIDER -> ProviderDialog(state, vm)
        BillingDialog.ADD_NODE    -> AddNodeDialog(state, vm)
        BillingDialog.UPDATE_DATE -> UpdateDateDialog(state, vm)
        BillingDialog.ADD_PAYMENT -> PaymentDialog(state, vm)
        null -> Unit
    }
    state.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = vm::dismissDelete,
            title = { Text(stringResource(Res.string.infra_delete_title)) },
            text = { Text(stringResource(Res.string.infra_delete_confirm, pending.label)) },
            confirmButton = {
                Button(
                    onClick = vm::confirmDelete,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(Res.string.common_delete)) }
            },
            dismissButton = { TextButton(onClick = vm::dismissDelete) { Text(stringResource(Res.string.common_cancel)) } },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            val refresh: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
                IconButton(onClick = vm::load) { Icon(Icons.Rounded.Refresh, contentDescription = stringResource(Res.string.common_refresh)) }
            }
            if (onBack != null) FwDetailTopBar(title = stringResource(Res.string.infra_title), onBack = onBack, actions = refresh)
            else FwTopBar(title = stringResource(Res.string.infra_title), actions = refresh)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = vm::openAddDialog,
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Rounded.Add, contentDescription = null) }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.billingNodes.isEmpty() && state.providers.isEmpty() && state.history.isEmpty() ->
                    Column(modifier = Modifier.fillMaxSize()) {
                        StatsGrid(state.stats)
                        BillingTabSelector(
                            activeTab = state.activeTab,
                            nodesCount = state.billingNodes.size,
                            historyCount = state.history.size,
                            providersCount = state.providers.size,
                            onSelect = vm::setTab,
                        )
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { WaveLoader() }
                    }

                state.error != null && state.stats == null ->
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(state.error!!.resolve(), color = MaterialTheme.colorScheme.error)
                        Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) { Text(stringResource(Res.string.common_retry)) }
                    }

                else -> PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = vm::load,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    BillingList(state, vm)
                }
            }
        }
    }
}

/**
 * Scrollable billing content: the overview stats scroll away with the list while the
 * Nodes / History / Providers tab selector stays pinned to the top.
 *
 * Both the overview and the selector are fixed overlays (not LazyColumn items), so the list's
 * overscroll stretch never distorts them. A single spacer item reserves the header region
 * (overview + gap + selector); the overlays are then positioned from the scroll offset:
 * the overview rides up and off with the scroll, while the selector follows it but clamps to
 * the top once the overview has scrolled away. Only the list rows show the overscroll effect.
 */
@Composable
private fun BillingList(state: InfraBillingUiState, vm: InfraBillingViewModel) {
    val emptyNodes = stringResource(Res.string.infra_empty_nodes)
    val emptyHistory = stringResource(Res.string.infra_empty_history)
    val emptyProviders = stringResource(Res.string.infra_empty_providers)
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val gapPx = with(density) { 8.dp.roundToPx() }
    var overviewHeightPx by remember { mutableStateOf(0) }
    var selectorHeightPx by remember { mutableStateOf(0) }
    val headerHeightPx = overviewHeightPx + gapPx + selectorHeightPx

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item(key = "header-space") {
                Spacer(Modifier.height(with(density) { headerHeightPx.toDp() }))
            }
            when (state.activeTab) {
                0 -> tabItems(state.billingNodes, emptyNodes, { it.uuid }) { node ->
                    BillingNodeItem(node, onClick = { vm.openUpdateDate(node) }, onDelete = {
                        vm.requestDelete(PendingDelete(PendingDelete.Kind.NODE, node.uuid, node.nodeName))
                    })
                }

                1 -> tabItems(state.history, emptyHistory, { it.uuid }) { rec ->
                    BillRecordItem(rec, onDelete = {
                        vm.requestDelete(PendingDelete(PendingDelete.Kind.RECORD, rec.uuid, rec.providerName))
                    })
                }

                else -> tabItems(state.providers, emptyProviders, { it.uuid }) { provider ->
                    ProviderItem(provider, onClick = { vm.openEditProvider(provider) }, onDelete = {
                        vm.requestDelete(PendingDelete(PendingDelete.Kind.PROVIDER, provider.uuid, provider.name))
                    })
                }
            }
        }

        // Top of the reserved header region in viewport coords (negative once scrolled past it).
        val headerOffsetY by remember(headerHeightPx) {
            derivedStateOf {
                if (listState.firstVisibleItemIndex == 0) -listState.firstVisibleItemScrollOffset
                else -headerHeightPx
            }
        }
        // Overview rides with the scroll and slides off the top.
        Box(
            modifier = Modifier
                .offset { IntOffset(0, headerOffsetY) }
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .onSizeChanged { overviewHeightPx = it.height },
        ) { StatsGrid(state.stats) }
        // Selector follows the overview but clamps to the top.
        Box(
            modifier = Modifier
                .offset { IntOffset(0, (headerOffsetY + overviewHeightPx + gapPx).coerceAtLeast(0)) }
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .onSizeChanged { selectorHeightPx = it.height },
        ) {
            BillingTabSelector(
                activeTab = state.activeTab,
                nodesCount = state.billingNodes.size,
                historyCount = state.history.size,
                providersCount = state.providers.size,
                onSelect = vm::setTab,
            )
        }
    }
}

// Stats grid (Dashboard LiveTile layout)

@Composable
private fun StatsGrid(stats: BillingStats?) {
    val fwStatus = LocalFwStatus.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Dns, tint = MaterialTheme.colorScheme.primary,
                label = stringResource(Res.string.infra_stat_nodes),
                value = (stats?.totalBillingNodes ?: 0).toString(),
            )
            StatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Event, tint = fwStatus.warning,
                label = stringResource(Res.string.infra_stat_upcoming),
                value = (stats?.upcomingCount ?: 0).toString(),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.CreditCard, tint = fwStatus.online,
                label = stringResource(Res.string.infra_stat_this_month),
                value = "$" + (stats?.currentMonthPayments ?: 0.0).format2(),
            )
            StatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Rounded.TrendingUp, tint = MaterialTheme.colorScheme.tertiary,
                label = stringResource(Res.string.infra_stat_total_spent),
                value = "$" + (stats?.totalSpent ?: 0.0).format2(),
            )
        }
    }
}

@Composable
private fun StatTile(modifier: Modifier, icon: ImageVector, tint: Color, label: String, value: String) {
    val monoFont = LocalFwMonoFont.current
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp)) }
            Text(
                label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp, maxLines = 1,
            )
            Text(
                value, style = MaterialTheme.typography.headlineSmall.copy(fontFamily = monoFont),
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1,
            )
        }
    }
}

// Tab selector (Squads pill style, three segments)

@Composable
private fun BillingTabSelector(
    activeTab: Int,
    nodesCount: Int,
    historyCount: Int,
    providersCount: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TabSegment(Modifier.weight(1f), activeTab == 0, stringResource(Res.string.infra_tab_nodes), nodesCount) { onSelect(0) }
        TabSegment(Modifier.weight(1f), activeTab == 1, stringResource(Res.string.infra_tab_history), historyCount) { onSelect(1) }
        TabSegment(Modifier.weight(1f), activeTab == 2, stringResource(Res.string.infra_tab_providers), providersCount) { onSelect(2) }
    }
}

@Composable
private fun TabSegment(modifier: Modifier, selected: Boolean, label: String, count: Int, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val bg by animateColorAsState(if (selected) accent else Color.Transparent, label = "infra_seg_bg")
    val content = if (selected) onAccent else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier.clip(RoundedCornerShape(percent = 50)).background(bg)
            .clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier.clip(RoundedCornerShape(percent = 50))
                .background(if (selected) onAccent.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 6.dp, vertical = 1.dp),
        ) { Text("$count", style = MaterialTheme.typography.labelSmall, color = content) }
    }
}

// Generic tab list — adds the active tab's rows (or an empty placeholder) into the shared LazyColumn.

private fun <T> LazyListScope.tabItems(
    items: List<T>,
    emptyText: String,
    key: (T) -> Any,
    itemContent: @Composable (T) -> Unit,
) {
    if (items.isEmpty()) {
        item(key = "empty") {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp), contentAlignment = Alignment.Center) {
                Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        items(items, key = key) { item ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) { itemContent(item) }
        }
    }
}

// Item cards

@Composable
private fun BillingNodeItem(node: BillingNode, onClick: () -> Unit, onDelete: () -> Unit) {
    val fwStatus = LocalFwStatus.current
    val monoFont = LocalFwMonoFont.current
    val remaining = expiryRemaining(node.nextBillingAt)
    val badgeColor = when (remaining) {
        ExpiryRemaining.Expired -> fwStatus.offline
        is ExpiryRemaining.Left -> if (remaining.unit == DurationUnit.DAYS && remaining.value <= 3) fwStatus.warning else fwStatus.online
        ExpiryRemaining.Infinite -> fwStatus.neutral
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(node.providerName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val flag = countryFlag(node.countryCode)
                Text(
                    if (flag.isNotBlank()) "$flag ${node.nodeName}" else node.nodeName,
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium,
                )
                Text(
                    stringResource(Res.string.infra_next_billing, formatDate(node.nextBillingAt)),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(percent = 50)).background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) { Text(remaining.localized(), style = MaterialTheme.typography.labelMedium, color = badgeColor, maxLines = 1) }
            OverflowDelete(onDelete)
        }
    }
}

@Composable
private fun BillRecordItem(rec: BillRecord, onDelete: () -> Unit) {
    val fwStatus = LocalFwStatus.current
    val monoFont = LocalFwMonoFont.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(rec.providerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(
                    formatDate(rec.billedAt),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "$" + rec.amount.format2(),
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = monoFont),
                color = fwStatus.online,
            )
            OverflowDelete(onDelete)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProviderItem(provider: InfraProvider, onClick: () -> Unit, onDelete: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(provider.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                OverflowDelete(onDelete)
            }
            provider.loginUrl?.takeIf { it.isNotBlank() }?.let { url ->
                Row(
                    modifier = Modifier.clickable { uriHandler.openUri(url) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(stringResource(Res.string.infra_login_url), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                "${stringResource(Res.string.infra_provider_nodes, provider.nodes.size)}  ·  " +
                    "${stringResource(Res.string.infra_provider_bills, provider.totalBills)}  ·  $" + provider.totalAmount.format2(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (provider.nodes.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    provider.nodes.forEach { ref ->
                        val flag = countryFlag(ref.countryCode)
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(percent = 50))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                if (flag.isNotBlank()) "$flag ${ref.name}" else ref.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverflowDelete(onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.common_delete), color = MaterialTheme.colorScheme.error) },
                onClick = { expanded = false; onDelete() },
            )
        }
    }
}

// Dialogs

@Composable
private fun ProviderDialog(state: InfraBillingUiState, vm: InfraBillingViewModel) {
    val isEdit = state.dialog == BillingDialog.EDIT_PROVIDER
    DialogScaffold(
        title = stringResource(if (isEdit) Res.string.infra_edit_provider else Res.string.infra_add_provider),
        confirmLabel = stringResource(if (isEdit) Res.string.common_save else Res.string.common_create),
        isLoading = state.dialogIsLoading,
        error = state.dialogError?.resolve(),
        onConfirm = vm::submitDialog,
        onDismiss = vm::dismissDialog,
    ) {
        OutlinedTextField(
            value = state.formName, onValueChange = vm::onName,
            label = { Text(stringResource(Res.string.infra_provider_name)) },
            singleLine = true, enabled = !state.dialogIsLoading, shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.formFavicon, onValueChange = vm::onFavicon,
            label = { Text(stringResource(Res.string.infra_favicon_link)) },
            singleLine = true, enabled = !state.dialogIsLoading, shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.formLogin, onValueChange = vm::onLogin,
            label = { Text(stringResource(Res.string.infra_login_url)) },
            singleLine = true, enabled = !state.dialogIsLoading, shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AddNodeDialog(state: InfraBillingUiState, vm: InfraBillingViewModel) {
    DialogScaffold(
        title = stringResource(Res.string.infra_add_node),
        confirmLabel = stringResource(Res.string.common_create),
        isLoading = state.dialogIsLoading,
        error = state.dialogError?.resolve(),
        confirmEnabled = state.availableNodes.isNotEmpty() && state.providers.isNotEmpty(),
        onConfirm = vm::submitDialog,
        onDismiss = vm::dismissDialog,
    ) {
        if (state.providers.isEmpty() || state.availableNodes.isEmpty()) {
            Text(stringResource(Res.string.infra_no_available_nodes), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        FormDropdown(
            label = stringResource(Res.string.infra_provider),
            selectedUuid = state.formProviderUuid,
            options = state.providers.map { it.uuid to it.name },
            onSelect = vm::onProvider,
        )
        FormDropdown(
            label = stringResource(Res.string.infra_node),
            selectedUuid = state.formNodeUuid,
            options = state.availableNodes.map { node ->
                val flag = countryFlag(node.countryCode)
                node.uuid to if (flag.isNotBlank()) "$flag ${node.name}" else node.name
            },
            onSelect = vm::onNode,
        )
        DateField(label = stringResource(Res.string.infra_billing_date), millis = state.formDateMillis, onPick = vm::onDate)
    }
}

@Composable
private fun UpdateDateDialog(state: InfraBillingUiState, vm: InfraBillingViewModel) {
    DialogScaffold(
        title = stringResource(Res.string.infra_update_billing_date),
        confirmLabel = stringResource(Res.string.common_save),
        isLoading = state.dialogIsLoading,
        error = state.dialogError?.resolve(),
        onConfirm = vm::submitDialog,
        onDismiss = vm::dismissDialog,
    ) {
        DateField(label = stringResource(Res.string.infra_billing_date), millis = state.formDateMillis, onPick = vm::onDate)
        DatePresets(current = state.formDateMillis, onPick = vm::onDate, enabled = !state.dialogIsLoading)
    }
}

@Composable
private fun PaymentDialog(state: InfraBillingUiState, vm: InfraBillingViewModel) {
    DialogScaffold(
        title = stringResource(Res.string.infra_add_payment),
        confirmLabel = stringResource(Res.string.common_create),
        isLoading = state.dialogIsLoading,
        error = state.dialogError?.resolve(),
        confirmEnabled = state.providers.isNotEmpty(),
        onConfirm = vm::submitDialog,
        onDismiss = vm::dismissDialog,
    ) {
        FormDropdown(
            label = stringResource(Res.string.infra_provider),
            selectedUuid = state.formProviderUuid,
            options = state.providers.map { it.uuid to it.name },
            onSelect = vm::onProvider,
        )
        DateField(label = stringResource(Res.string.infra_payment_date), millis = state.formDateMillis, onPick = vm::onDate)
        OutlinedTextField(
            value = state.formAmount, onValueChange = vm::onAmount,
            label = { Text(stringResource(Res.string.infra_amount)) },
            singleLine = true, enabled = !state.dialogIsLoading, shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Shared AlertDialog wrapper for the billing forms. */
@Composable
private fun DialogScaffold(
    title: String,
    confirmLabel: String,
    isLoading: Boolean,
    error: String?,
    confirmEnabled: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isLoading && confirmEnabled) {
                if (isLoading) WaveLoader(modifier = Modifier.size(width = 26.dp, height = 16.dp))
                else Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(Res.string.common_cancel)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDropdown(label: String, selectedUuid: String?, options: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = options.find { it.first == selectedUuid }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = displayText, onValueChange = {}, readOnly = true,
            label = { Text(label) }, shape = MaterialTheme.shapes.medium,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (uuid, optionLabel) ->
                DropdownMenuItem(text = { Text(optionLabel) }, onClick = { onSelect(uuid); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, millis: Long, onPick: (Long) -> Unit) {
    var show by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formatDate(Instant.fromEpochMilliseconds(millis).toString()),
            onValueChange = {}, readOnly = true, enabled = true,
            label = { Text(label) }, shape = MaterialTheme.shapes.medium,
            leadingIcon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier.matchParentSize().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { show = true }
        )
    }
    if (show) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { picked ->
                        // DatePicker reports UTC midnight; reinterpret that calendar day in the device zone.
                        val tz = TimeZone.currentSystemDefault()
                        val date = Instant.fromEpochMilliseconds(picked).toLocalDateTime(TimeZone.UTC).date
                        onPick(date.atStartOfDayIn(tz).toEpochMilliseconds())
                    }
                    show = false
                }) { Text(stringResource(Res.string.common_ok)) }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text(stringResource(Res.string.common_cancel)) } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun DatePresets(current: Long, onPick: (Long) -> Unit, enabled: Boolean) {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Preset(Res.string.infra_preset_today, Modifier.weight(1f), enabled) { onPick(today.atStartOfDayIn(tz).toEpochMilliseconds()) }
        Preset(Res.string.infra_preset_tomorrow, Modifier.weight(1f), enabled) { onPick(today.plus(DatePeriod(days = 1)).atStartOfDayIn(tz).toEpochMilliseconds()) }
        Preset(Res.string.infra_preset_month, Modifier.weight(1f), enabled) {
            val base = Instant.fromEpochMilliseconds(current).toLocalDateTime(tz).date
            onPick(base.plus(DatePeriod(months = 1)).atStartOfDayIn(tz).toEpochMilliseconds())
        }
    }
}

@Composable
private fun Preset(label: StringResource, modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier, shape = RoundedCornerShape(percent = 50)) {
        Text(stringResource(label), style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}
