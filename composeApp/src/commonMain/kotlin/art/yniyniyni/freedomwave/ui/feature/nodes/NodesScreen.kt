@file:OptIn(ExperimentalTransitionApi::class)

package art.yniyniyni.freedomwave.ui.feature.nodes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import art.yniyniyni.freedomwave.ui.navigation.BackGestureEffect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import art.yniyniyni.freedomwave.ui.components.FwDetailTopBar
import art.yniyniyni.freedomwave.ui.components.FwTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.domain.model.Node
import art.yniyniyni.freedomwave.domain.model.NodeStatus
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_retry
import freedomwave.composeapp.generated.resources.nodes_detail_address
import freedomwave.composeapp.generated.resources.nodes_detail_arch
import freedomwave.composeapp.generated.resources.nodes_detail_connection
import freedomwave.composeapp.generated.resources.nodes_detail_country
import freedomwave.composeapp.generated.resources.nodes_detail_cpu
import freedomwave.composeapp.generated.resources.nodes_detail_cpu_cores
import freedomwave.composeapp.generated.resources.nodes_detail_cpu_model
import freedomwave.composeapp.generated.resources.nodes_detail_created
import freedomwave.composeapp.generated.resources.nodes_detail_hardware
import freedomwave.composeapp.generated.resources.nodes_detail_hostname
import freedomwave.composeapp.generated.resources.nodes_detail_limit
import freedomwave.composeapp.generated.resources.nodes_detail_load
import freedomwave.composeapp.generated.resources.nodes_detail_memory
import freedomwave.composeapp.generated.resources.nodes_detail_memory_free
import freedomwave.composeapp.generated.resources.nodes_detail_memory_total
import freedomwave.composeapp.generated.resources.nodes_detail_metadata
import freedomwave.composeapp.generated.resources.nodes_detail_multiplier
import freedomwave.composeapp.generated.resources.nodes_detail_no_traffic_data
import freedomwave.composeapp.generated.resources.nodes_detail_node_version
import freedomwave.composeapp.generated.resources.nodes_detail_notify_at
import freedomwave.composeapp.generated.resources.nodes_detail_online_users
import freedomwave.composeapp.generated.resources.nodes_detail_platform
import freedomwave.composeapp.generated.resources.nodes_detail_port
import freedomwave.composeapp.generated.resources.nodes_detail_ram
import freedomwave.composeapp.generated.resources.nodes_detail_reset_day
import freedomwave.composeapp.generated.resources.nodes_detail_reset_day_value
import freedomwave.composeapp.generated.resources.nodes_detail_software
import freedomwave.composeapp.generated.resources.nodes_detail_status
import freedomwave.composeapp.generated.resources.nodes_detail_status_changed
import freedomwave.composeapp.generated.resources.nodes_detail_tags
import freedomwave.composeapp.generated.resources.nodes_detail_tracking
import freedomwave.composeapp.generated.resources.nodes_detail_tracking_active
import freedomwave.composeapp.generated.resources.nodes_detail_tracking_inactive
import freedomwave.composeapp.generated.resources.nodes_detail_traffic
import freedomwave.composeapp.generated.resources.nodes_detail_traffic_history
import freedomwave.composeapp.generated.resources.nodes_detail_unlimited
import freedomwave.composeapp.generated.resources.nodes_detail_updated
import freedomwave.composeapp.generated.resources.nodes_detail_uptime
import freedomwave.composeapp.generated.resources.nodes_detail_used
import freedomwave.composeapp.generated.resources.nodes_detail_uuid
import freedomwave.composeapp.generated.resources.nodes_detail_xray_uptime
import freedomwave.composeapp.generated.resources.nodes_detail_xray_version
import freedomwave.composeapp.generated.resources.nodes_disable
import freedomwave.composeapp.generated.resources.nodes_empty
import freedomwave.composeapp.generated.resources.nodes_enable
import freedomwave.composeapp.generated.resources.common_refresh
import freedomwave.composeapp.generated.resources.nodes_reset_traffic
import freedomwave.composeapp.generated.resources.nodes_restart
import freedomwave.composeapp.generated.resources.nodes_status_connecting
import freedomwave.composeapp.generated.resources.nodes_status_disabled
import freedomwave.composeapp.generated.resources.nodes_status_offline
import freedomwave.composeapp.generated.resources.nodes_status_online
import freedomwave.composeapp.generated.resources.nodes_title_count
import art.yniyniyni.freedomwave.ui.components.BandwidthChart
import art.yniyniyni.freedomwave.ui.components.ChartSeries
import art.yniyniyni.freedomwave.ui.components.ShimmerList
import art.yniyniyni.freedomwave.ui.feature.bandwidth.BandwidthUiState
import art.yniyniyni.freedomwave.ui.feature.bandwidth.BandwidthViewModel
import art.yniyniyni.freedomwave.ui.feature.bandwidth.TimeRange
import art.yniyniyni.freedomwave.ui.feature.bandwidth.label
import art.yniyniyni.freedomwave.ui.l10n.localized
import art.yniyniyni.freedomwave.ui.l10n.localizedBytes
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import art.yniyniyni.freedomwave.ui.theme.LocalFwStatus
import art.yniyniyni.freedomwave.util.countryFlag
import art.yniyniyni.freedomwave.util.format2
import art.yniyniyni.freedomwave.util.relativePast
import art.yniyniyni.freedomwave.util.uptimeParts
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private sealed interface NodesNav {
    object List : NodesNav
    data class Detail(val node: Node) : NodesNav
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodesScreen(
    vm: NodesViewModel = koinViewModel(),
    bandwidthVm: BandwidthViewModel = koinViewModel()
) {
    val state by vm.state.collectAsState()
    val bandwidthState by bandwidthVm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var nav: NodesNav by remember { mutableStateOf(NodesNav.List) }

    val actionErrorText = state.actionError?.resolve()
    LaunchedEffect(actionErrorText) {
        actionErrorText?.let { snackbar.showSnackbar(it); vm.clearActionError() }
    }

    val isDetail = nav is NodesNav.Detail

    val transitionState = remember { SeekableTransitionState(false) }
    val transition = rememberTransition(transitionState, label = "nodes_nav")

    LaunchedEffect(isDetail) { transitionState.animateTo(isDetail) }

    var lastDetailNode by remember { mutableStateOf<Node?>(null) }
    val currentDetailNode = (nav as? NodesNav.Detail)?.node
    if (currentDetailNode != null) lastDetailNode = currentDetailNode

    BackGestureEffect(
        enabled    = isDetail,
        onProgress = { fraction -> transitionState.seekTo(fraction, false) },
        onCommit   = { transitionState.animateTo(false); nav = NodesNav.List },
        onCancel   = { transitionState.animateTo(true) },
    )

    transition.AnimatedContent(
        contentKey = { it },
        transitionSpec = {
            if (targetState) {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 4 }
            } else {
                slideInHorizontally { -it / 4 } togetherWith slideOutHorizontally { it }
            }.apply { targetContentZIndex = if (targetState) 1f else 0f }
        },
    ) { showDetail ->
        if (showDetail) {
            lastDetailNode?.let { node ->
                NodeDetailScreen(
                    node           = node,
                    bandwidthState = bandwidthState,
                    onRangeChange  = bandwidthVm::setRange,
                    onBack         = { nav = NodesNav.List },
                    onEnable       = { vm.enableNode(node.uuid) },
                    onDisable      = { vm.disableNode(node.uuid) },
                    onRestart      = { vm.restartNode(node.uuid) },
                    onReset        = { vm.resetTraffic(node.uuid) },
                )
            }
        } else {
            Scaffold(
                contentWindowInsets = WindowInsets(0),
                snackbarHost = { SnackbarHost(snackbar) },
                topBar = {
                    FwTopBar(
                        title   = stringResource(Res.string.nodes_title_count, state.nodes.size),
                        actions = { IconButton(onClick = vm::load) { Icon(Icons.Rounded.Refresh, contentDescription = stringResource(Res.string.common_refresh)) } },
                    )
                },
            ) { padding ->
                when {
                    state.isLoading && state.nodes.isEmpty() ->
                        ShimmerList(modifier = Modifier.padding(padding))

                    state.error != null && state.nodes.isEmpty() ->
                        Column(
                            Modifier.fillMaxSize().padding(padding).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(state.error!!.resolve(), color = MaterialTheme.colorScheme.error)
                            Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) { Text(stringResource(Res.string.common_retry)) }
                        }

                    else ->
                        PullToRefreshBox(
                            isRefreshing = state.isLoading && state.nodes.isNotEmpty(),
                            onRefresh    = vm::load,
                            modifier     = Modifier.fillMaxSize().padding(padding),
                        ) {
                            if (state.nodes.isEmpty()) {
                                Text(
                                    stringResource(Res.string.nodes_empty),
                                    modifier = Modifier.align(Alignment.Center),
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                LazyColumn(
                                    contentPadding      = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    items(state.nodes, key = { it.uuid }) { node ->
                                        NodeListItem(
                                            node    = node,
                                            onClick = { nav = NodesNav.Detail(node) },
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

@Composable
private fun NodeListItem(node: Node, onClick: () -> Unit) {
    val monoFont = LocalFwMonoFont.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NodeStatusDot(node.status)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(node.name, style = MaterialTheme.typography.titleSmall)
                val countryDisplay = if (node.countryCode.isNotBlank())
                    "${node.countryCode} ${countryFlag(node.countryCode)}"
                else ""
                val address = "${node.address}${node.port?.let { ":$it" } ?: ""}"
                Text(
                    if (countryDisplay.isNotBlank()) "$countryDisplay · $address" else address,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val metrics = if (node.isOnline) {
                    listOfNotNull(
                        node.usersOnline.takeIf { it > 0 }?.let { stringResource(Res.string.nodes_detail_online_users, it) },
                        node.cpuLoadPercent?.let { "${stringResource(Res.string.nodes_detail_cpu)} ${it.roundToInt()}%" },
                        node.memoryUsedPercent?.let { "${stringResource(Res.string.nodes_detail_memory)} ${it.roundToInt()}%" },
                    ).joinToString(" · ")
                } else ""
                Text(
                    metrics.ifBlank { localizedBytes(node.trafficUsedBytes) },
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun nodeStatusLabel(status: NodeStatus): String = stringResource(
    when (status) {
        NodeStatus.ONLINE     -> Res.string.nodes_status_online
        NodeStatus.OFFLINE    -> Res.string.nodes_status_offline
        NodeStatus.DISABLED   -> Res.string.nodes_status_disabled
        NodeStatus.CONNECTING -> Res.string.nodes_status_connecting
    }
)

@Composable
private fun NodeStatusDot(status: NodeStatus) {
    val fwStatus = LocalFwStatus.current
    val color = when (status) {
        NodeStatus.ONLINE     -> fwStatus.online
        NodeStatus.OFFLINE    -> fwStatus.offline
        NodeStatus.DISABLED   -> fwStatus.neutral
        NodeStatus.CONNECTING -> fwStatus.warning
    }
    Surface(modifier = Modifier.size(12.dp), shape = CircleShape, color = color) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NodeDetailScreen(
    node: Node,
    bandwidthState: BandwidthUiState,
    onRangeChange: (TimeRange) -> Unit,
    onBack: () -> Unit,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onRestart: () -> Unit,
    onReset: () -> Unit
) {
    val monoFont = LocalFwMonoFont.current
    val nodeSeries = bandwidthState.data?.series
        ?.find { it.uuid == node.uuid }
        ?.let { listOf(ChartSeries(it.name, it.color, it.data)) }
        ?: emptyList()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { FwDetailTopBar(title = node.name, onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { NodeHeaderCard(node) }

            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.nodes_detail_connection), Icons.Rounded.Wifi)
                    DetailRow(stringResource(Res.string.nodes_detail_address), node.address, monoFont)
                    node.port?.let { DetailRow(stringResource(Res.string.nodes_detail_port), it.toString(), monoFont) }
                    DetailRow(stringResource(Res.string.nodes_detail_status), nodeStatusLabel(node.status), monoFont)
                    if (node.lastStatusChange != null) {
                        DetailRow(stringResource(Res.string.nodes_detail_status_changed), relativePast(node.lastStatusChange).localized(), monoFont)
                    }
                }
            }

            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.nodes_detail_traffic), Icons.Rounded.SwapVert)
                    val limit = node.trafficLimitBytes
                    if (limit != null && limit > 0) {
                        val pct = (node.trafficUsedBytes.toFloat() / limit.toFloat() * 100f).coerceIn(0f, 100f)
                        MiniGauge(
                            label    = stringResource(Res.string.nodes_detail_used),
                            percent  = pct,
                            trailing = "${localizedBytes(node.trafficUsedBytes)} / ${localizedBytes(limit)}",
                        )
                    } else {
                        DetailRow(stringResource(Res.string.nodes_detail_used), localizedBytes(node.trafficUsedBytes), monoFont)
                        DetailRow(stringResource(Res.string.nodes_detail_limit), stringResource(Res.string.nodes_detail_unlimited), monoFont)
                    }
                    DetailRow(
                        stringResource(Res.string.nodes_detail_tracking),
                        stringResource(if (node.isTrafficTrackingActive) Res.string.nodes_detail_tracking_active else Res.string.nodes_detail_tracking_inactive),
                        monoFont,
                    )
                    node.trafficResetDay?.let { DetailRow(stringResource(Res.string.nodes_detail_reset_day), stringResource(Res.string.nodes_detail_reset_day_value, it), monoFont) }
                    node.notifyPercent?.let { DetailRow(stringResource(Res.string.nodes_detail_notify_at), "$it%", monoFont) }
                    DetailRow(stringResource(Res.string.nodes_detail_multiplier), "${node.consumptionMultiplier}×", monoFont)
                }
            }

            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.nodes_detail_traffic_history), Icons.Rounded.ShowChart)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        TimeRange.entries.forEachIndexed { index, range ->
                            SegmentedButton(
                                selected = bandwidthState.selectedRange == range,
                                onClick  = { onRangeChange(range) },
                                shape    = SegmentedButtonDefaults.itemShape(index, TimeRange.entries.size),
                                label    = { Text(range.label()) }
                            )
                        }
                    }
                    when {
                        bandwidthState.isLoading ->
                            CircularProgressIndicator(
                                modifier = Modifier.fillMaxWidth().padding(16.dp).wrapContentWidth(Alignment.CenterHorizontally),
                                color    = MaterialTheme.colorScheme.primary,
                            )

                        nodeSeries.isEmpty() && bandwidthState.data != null ->
                            Text(
                                stringResource(Res.string.nodes_detail_no_traffic_data),
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                        nodeSeries.isNotEmpty() ->
                            BandwidthChart(
                                categories = bandwidthState.data?.categories ?: emptyList(),
                                series     = nodeSeries
                            )
                    }
                }
            }

            if (node.xrayVersion != null || node.nodeVersion != null || node.xrayUptimeSeconds != null) {
                item {
                    FwExpandableCard(stringResource(Res.string.nodes_detail_software), Icons.Rounded.Code) {
                        node.xrayVersion?.let { DetailRow(stringResource(Res.string.nodes_detail_xray_version), it, monoFont) }
                        node.nodeVersion?.let { DetailRow(stringResource(Res.string.nodes_detail_node_version), it, monoFont) }
                        node.xrayUptimeSeconds?.let { DetailRow(stringResource(Res.string.nodes_detail_xray_uptime), uptimeParts(it).localized(), monoFont) }
                    }
                }
            }

            if (node.cpuModel != null || node.cpus != null || node.memoryTotalBytes != null ||
                node.memoryFreeBytes != null || node.arch != null || node.platform != null ||
                node.uptimeSeconds != null || node.loadAvg1 != null || node.hostname != null) {
                item {
                    FwExpandableCard(stringResource(Res.string.nodes_detail_hardware), Icons.Rounded.Memory) {
                        node.cpuModel?.takeIf { it.isNotBlank() }?.let { DetailRow(stringResource(Res.string.nodes_detail_cpu_model), it, monoFont) }
                        node.cpus?.let { DetailRow(stringResource(Res.string.nodes_detail_cpu_cores), it.toString(), monoFont) }
                        node.arch?.takeIf { it.isNotBlank() }?.let { DetailRow(stringResource(Res.string.nodes_detail_arch), it, monoFont) }
                        node.platform?.takeIf { it.isNotBlank() }?.let { DetailRow(stringResource(Res.string.nodes_detail_platform), it, monoFont) }
                        node.memoryTotalBytes?.let { DetailRow(stringResource(Res.string.nodes_detail_memory_total), localizedBytes(it), monoFont) }
                        node.memoryUsedBytes?.let { DetailRow(stringResource(Res.string.nodes_detail_ram), localizedBytes(it), monoFont) }
                        node.memoryFreeBytes?.let { DetailRow(stringResource(Res.string.nodes_detail_memory_free), localizedBytes(it), monoFont) }
                        node.uptimeSeconds?.let { DetailRow(stringResource(Res.string.nodes_detail_uptime), uptimeParts(it).localized(), monoFont) }
                        node.loadAvg1?.let { DetailRow(stringResource(Res.string.nodes_detail_load), it.toDouble().format2(), monoFont) }
                        node.hostname?.let { DetailRow(stringResource(Res.string.nodes_detail_hostname), it, monoFont) }
                    }
                }
            }

            item {
                FwExpandableCard(stringResource(Res.string.nodes_detail_metadata), Icons.Rounded.Info) {
                    DetailRow(stringResource(Res.string.nodes_detail_uuid), node.uuid, monoFont)
                    if (node.countryCode.isNotBlank()) DetailRow(stringResource(Res.string.nodes_detail_country), "${node.countryCode} ${countryFlag(node.countryCode)}", monoFont)
                    if (node.tags.isNotEmpty()) DetailRow(stringResource(Res.string.nodes_detail_tags), node.tags.joinToString(", "), monoFont)
                    DetailRow(stringResource(Res.string.nodes_detail_created), relativePast(node.createdAt).localized(), monoFont)
                    DetailRow(stringResource(Res.string.nodes_detail_updated), relativePast(node.updatedAt).localized(), monoFont)
                }
            }

            item { NodeActions(node, onEnable, onDisable, onRestart, onReset) }
        }
    }
}

@Composable
private fun NodeHeaderCard(node: Node) {
    val fwStatus = LocalFwStatus.current
    val statusColor = when (node.status) {
        NodeStatus.ONLINE     -> fwStatus.online
        NodeStatus.OFFLINE    -> fwStatus.offline
        NodeStatus.DISABLED   -> fwStatus.neutral
        NodeStatus.CONNECTING -> fwStatus.warning
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NodeStatusDot(node.status)
                        Text(nodeStatusLabel(node.status).uppercase(), style = MaterialTheme.typography.titleSmall, color = statusColor)
                    }
                    OnlinePill(node.usersOnline)
                }
                if (node.xrayVersion != null || node.nodeVersion != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        node.xrayVersion?.let { VersionChip(Icons.Rounded.Bolt, "Xray $it", MaterialTheme.colorScheme.primary) }
                        node.nodeVersion?.let { VersionChip(Icons.Rounded.Dns, "Node $it", MaterialTheme.colorScheme.secondary) }
                    }
                }
                node.cpuLoadPercent?.let { cpu ->
                    val loadTrailing = listOfNotNull(
                        node.loadAvg1?.let { "1m ${it.toDouble().format2()}" },
                        node.loadAvg5?.let { "5m ${it.toDouble().format2()}" },
                        node.loadAvg15?.let { "15m ${it.toDouble().format2()}" },
                    ).joinToString(" · ")
                    MiniGauge(stringResource(Res.string.nodes_detail_cpu), cpu, loadTrailing)
                }
                node.memoryUsedPercent?.let { mem ->
                    val used = node.memoryUsedBytes
                    val total = node.memoryTotalBytes
                    val memTrailing = if (used != null && total != null) "${localizedBytes(used)} / ${localizedBytes(total)}" else ""
                    MiniGauge(stringResource(Res.string.nodes_detail_memory), mem, memTrailing)
                }
                if (!node.isOnline) {
                    node.lastStatusMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
    }
}

@Composable
private fun MiniGauge(label: String, percent: Float, trailing: String) {
    val fwStatus = LocalFwStatus.current
    val barColor = if (percent >= 90f) fwStatus.warning else fwStatus.online
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${percent.roundToInt()}%", style = MaterialTheme.typography.bodyMedium)
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50))
                .background(barColor.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth((percent / 100f).coerceIn(0f, 1f)).fillMaxHeight()
                    .clip(RoundedCornerShape(50)).background(barColor),
            )
        }
        if (trailing.isNotBlank()) {
            Text(trailing, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OnlinePill(count: Int) {
    val fwStatus = LocalFwStatus.current
    Row(
        modifier = Modifier.clip(RoundedCornerShape(50)).background(fwStatus.online.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Rounded.Person, contentDescription = null, tint = fwStatus.online, modifier = Modifier.size(14.dp))
        Text(stringResource(Res.string.nodes_detail_online_users, count), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun VersionChip(icon: ImageVector, text: String, tint: Color) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(50)).background(tint.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun FwExpandableCard(
    title: String,
    icon: ImageVector? = null,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DetailSectionTitle(title, icon)
                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation),
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) { content() }
            }
        }
    }
}

@Composable
private fun NodeActions(
    node: Node,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onRestart: () -> Unit,
    onReset: () -> Unit,
) {
    val actionPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            node.isOnline -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onDisable,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(percent = 50),
                    contentPadding = actionPadding,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                ) { Text(stringResource(Res.string.nodes_disable), textAlign = TextAlign.Center, maxLines = 2) }
                Button(
                    onClick = onRestart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(percent = 50),
                    contentPadding = actionPadding,
                ) { Text(stringResource(Res.string.nodes_restart), textAlign = TextAlign.Center, maxLines = 2) }
            }
            node.isDisabled -> Button(
                onClick = onEnable,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(percent = 50),
            ) { Text(stringResource(Res.string.nodes_enable)) }
            else -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEnable,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(percent = 50),
                    contentPadding = actionPadding,
                ) { Text(stringResource(Res.string.nodes_enable), textAlign = TextAlign.Center, maxLines = 2) }
                Button(
                    onClick = onRestart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(percent = 50),
                    contentPadding = actionPadding,
                ) { Text(stringResource(Res.string.nodes_restart), textAlign = TextAlign.Center, maxLines = 2) }
            }
        }
        Button(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor   = MaterialTheme.colorScheme.onSurface,
            )
        ) { Text(stringResource(Res.string.nodes_reset_traffic)) }
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
private fun DetailSectionTitle(title: String, icon: ImageVector? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(title, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun DetailRow(label: String, value: String, monoFont: androidx.compose.ui.text.font.FontFamily) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = monoFont),
        )
    }
}
