@file:OptIn(ExperimentalTransitionApi::class)

package art.yniyniyni.freedomwave.ui.feature.nodes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import art.yniyniyni.freedomwave.ui.navigation.BackGestureEffect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.domain.model.Node
import art.yniyniyni.freedomwave.domain.model.NodeStatus
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_retry
import freedomwave.composeapp.generated.resources.nodes_detail_address
import freedomwave.composeapp.generated.resources.nodes_detail_country
import freedomwave.composeapp.generated.resources.nodes_detail_cpus
import freedomwave.composeapp.generated.resources.nodes_detail_hostname
import freedomwave.composeapp.generated.resources.nodes_detail_info
import freedomwave.composeapp.generated.resources.nodes_detail_limit
import freedomwave.composeapp.generated.resources.nodes_detail_load
import freedomwave.composeapp.generated.resources.nodes_detail_message
import freedomwave.composeapp.generated.resources.nodes_detail_no_traffic_data
import freedomwave.composeapp.generated.resources.nodes_detail_ram
import freedomwave.composeapp.generated.resources.nodes_detail_status
import freedomwave.composeapp.generated.resources.nodes_detail_system
import freedomwave.composeapp.generated.resources.nodes_detail_tags
import freedomwave.composeapp.generated.resources.nodes_detail_traffic
import freedomwave.composeapp.generated.resources.nodes_detail_traffic_history
import freedomwave.composeapp.generated.resources.nodes_detail_unlimited
import freedomwave.composeapp.generated.resources.nodes_detail_uptime
import freedomwave.composeapp.generated.resources.nodes_detail_used
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
import art.yniyniyni.freedomwave.util.uptimeParts
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
                        actions = { TextButton(onClick = vm::load) { Text(stringResource(Res.string.common_refresh)) } },
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
                Text(
                    localizedBytes(node.trafficUsedBytes),
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
            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.nodes_detail_info))
                    DetailRow(stringResource(Res.string.nodes_detail_status), nodeStatusLabel(node.status), monoFont)
                    DetailRow(stringResource(Res.string.nodes_detail_address), "${node.address}${node.port?.let { ":$it" } ?: ""}", monoFont)
                    if (node.countryCode.isNotBlank()) {
                        DetailRow(stringResource(Res.string.nodes_detail_country), "${node.countryCode} ${countryFlag(node.countryCode)}", monoFont)
                    }
                    if (node.tags.isNotEmpty()) DetailRow(stringResource(Res.string.nodes_detail_tags), node.tags.joinToString(", "), monoFont)
                    node.lastStatusMessage?.let { DetailRow(stringResource(Res.string.nodes_detail_message), it, monoFont) }
                }
            }
            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.nodes_detail_traffic))
                    DetailRow(stringResource(Res.string.nodes_detail_used), localizedBytes(node.trafficUsedBytes), monoFont)
                    DetailRow(stringResource(Res.string.nodes_detail_limit), node.trafficLimitBytes?.let { localizedBytes(it) } ?: stringResource(Res.string.nodes_detail_unlimited), monoFont)
                }
            }

            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.nodes_detail_traffic_history))
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

            node.hostname?.let { hostname ->
                item {
                    FwDetailCard {
                        DetailSectionTitle(stringResource(Res.string.nodes_detail_system))
                        DetailRow(stringResource(Res.string.nodes_detail_hostname), hostname, monoFont)
                        node.cpus?.let { DetailRow(stringResource(Res.string.nodes_detail_cpus), it.toString(), monoFont) }
                        node.memoryUsedBytes?.let { used ->
                            node.memoryTotalBytes?.let { total ->
                                DetailRow(stringResource(Res.string.nodes_detail_ram), "${localizedBytes(used)} / ${localizedBytes(total)}", monoFont)
                            }
                        }
                        node.uptimeSeconds?.let { DetailRow(stringResource(Res.string.nodes_detail_uptime), uptimeParts(it).localized(), monoFont) }
                        node.loadAvg?.let { DetailRow(stringResource(Res.string.nodes_detail_load), it.toDouble().format2(), monoFont) }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (node.isOnline) {
                        Button(
                            onClick = onDisable,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        ) { Text(stringResource(Res.string.nodes_disable)) }
                        Button(
                            onClick = onRestart,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                        ) { Text(stringResource(Res.string.nodes_restart)) }
                    } else if (node.isDisabled) {
                        Button(
                            onClick = onEnable,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                        ) { Text(stringResource(Res.string.nodes_enable)) }
                    } else {
                        Button(
                            onClick = onEnable,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                        ) { Text(stringResource(Res.string.nodes_enable)) }
                        Button(
                            onClick = onRestart,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                        ) { Text(stringResource(Res.string.nodes_restart)) }
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
