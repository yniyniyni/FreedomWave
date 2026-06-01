package art.yniyniyni.freedomwave.ui.feature.nodes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import art.yniyniyni.freedomwave.ui.components.BandwidthChart
import art.yniyniyni.freedomwave.ui.components.ChartSeries
import art.yniyniyni.freedomwave.ui.components.ShimmerList
import art.yniyniyni.freedomwave.ui.feature.bandwidth.BandwidthUiState
import art.yniyniyni.freedomwave.ui.feature.bandwidth.BandwidthViewModel
import art.yniyniyni.freedomwave.ui.feature.bandwidth.TimeRange
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import art.yniyniyni.freedomwave.ui.theme.LocalFwStatus
import art.yniyniyni.freedomwave.util.formatBytes
import art.yniyniyni.freedomwave.util.formatUptime
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

    LaunchedEffect(state.actionError) {
        state.actionError?.let { snackbar.showSnackbar(it); vm.clearActionError() }
    }

    when (val current = nav) {
        is NodesNav.List ->
            Scaffold(
                snackbarHost = { SnackbarHost(snackbar) },
                topBar = {
                    TopAppBar(
                        title = { Text("Nodes (${state.nodes.size})") },
                        actions = { TextButton(onClick = vm::load) { Text("Refresh") } }
                    )
                }
            ) { padding ->
                when {
                    state.isLoading && state.nodes.isEmpty() ->
                        ShimmerList(modifier = Modifier.padding(padding))

                    state.error != null && state.nodes.isEmpty() ->
                        Column(
                            Modifier.fillMaxSize().padding(padding).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
                        }

                    else ->
                        PullToRefreshBox(
                            isRefreshing = state.isLoading && state.nodes.isNotEmpty(),
                            onRefresh = vm::load,
                            modifier = Modifier.fillMaxSize().padding(padding)
                        ) {
                            if (state.nodes.isEmpty()) {
                                Text(
                                    "No nodes found",
                                    modifier = Modifier.align(Alignment.Center),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.nodes, key = { it.uuid }) { node ->
                                        NodeListItem(
                                            node = node,
                                            onClick = { nav = NodesNav.Detail(node) },
                                        )
                                    }
                                }
                            }
                        }
                }
            }

        is NodesNav.Detail ->
            NodeDetailScreen(
                node = current.node,
                bandwidthState = bandwidthState,
                onRangeChange = bandwidthVm::setRange,
                onBack     = { nav = NodesNav.List },
                onEnable   = { vm.enableNode(current.node.uuid) },
                onDisable  = { vm.disableNode(current.node.uuid) },
                onRestart  = { vm.restartNode(current.node.uuid) },
                onReset    = { vm.resetTraffic(current.node.uuid) }
            )
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
                    formatBytes(node.trafficUsedBytes),
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

private fun countryFlag(code: String): String {
    if (code.length != 2) return ""
    val base = 0x1F1E6 - 'A'.code
    return String(Character.toChars(base + code[0].uppercaseChar().code)) +
           String(Character.toChars(base + code[1].uppercaseChar().code))
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
        topBar = {
            TopAppBar(
                title = { Text(node.name) },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FwDetailCard {
                    DetailSectionTitle("Node Info")
                    DetailRow("Status", node.status.name, monoFont)
                    DetailRow("Address", "${node.address}${node.port?.let { ":$it" } ?: ""}", monoFont)
                    if (node.countryCode.isNotBlank()) {
                        DetailRow("Country", "${node.countryCode} ${countryFlag(node.countryCode)}", monoFont)
                    }
                    if (node.tags.isNotEmpty()) DetailRow("Tags", node.tags.joinToString(", "), monoFont)
                    node.lastStatusMessage?.let { DetailRow("Message", it, monoFont) }
                }
            }
            item {
                FwDetailCard {
                    DetailSectionTitle("Traffic")
                    DetailRow("Used", formatBytes(node.trafficUsedBytes), monoFont)
                    DetailRow("Limit", node.trafficLimitBytes?.let { formatBytes(it) } ?: "Unlimited", monoFont)
                }
            }

            item {
                FwDetailCard {
                    DetailSectionTitle("Traffic History")
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        TimeRange.entries.forEachIndexed { index, range ->
                            SegmentedButton(
                                selected = bandwidthState.selectedRange == range,
                                onClick  = { onRangeChange(range) },
                                shape    = SegmentedButtonDefaults.itemShape(index, TimeRange.entries.size),
                                label    = { Text(range.label) }
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
                                "No traffic data for this node",
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
                        DetailSectionTitle("System")
                        DetailRow("Hostname", hostname, monoFont)
                        node.cpus?.let { DetailRow("CPUs", it.toString(), monoFont) }
                        node.memoryUsedBytes?.let { used ->
                            node.memoryTotalBytes?.let { total ->
                                DetailRow("RAM", "${formatBytes(used)} / ${formatBytes(total)}", monoFont)
                            }
                        }
                        node.uptimeSeconds?.let { DetailRow("Uptime", formatUptime(it), monoFont) }
                        node.loadAvg?.let { DetailRow("Load", "%.2f".format(it), monoFont) }
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
                        ) { Text("Disable Node") }
                        Button(
                            onClick = onRestart,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                        ) { Text("Restart Node") }
                    } else if (node.isDisabled) {
                        Button(
                            onClick = onEnable,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                        ) { Text("Enable Node") }
                    } else {
                        Button(
                            onClick = onEnable,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                        ) { Text("Enable Node") }
                        Button(
                            onClick = onRestart,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                        ) { Text("Restart Node") }
                    }
                    Button(
                        onClick = onReset,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor   = MaterialTheme.colorScheme.onSurface,
                        )
                    ) { Text("Reset Traffic") }
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
