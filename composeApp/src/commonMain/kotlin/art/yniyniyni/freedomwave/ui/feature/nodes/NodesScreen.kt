package art.yniyniyni.freedomwave.ui.feature.nodes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.domain.model.Node
import art.yniyniyni.freedomwave.domain.model.NodeStatus
import art.yniyniyni.freedomwave.ui.components.BandwidthChart
import art.yniyniyni.freedomwave.ui.components.ChartSeries
import art.yniyniyni.freedomwave.ui.components.ShimmerList
import art.yniyniyni.freedomwave.ui.feature.bandwidth.BandwidthUiState
import art.yniyniyni.freedomwave.ui.feature.bandwidth.BandwidthViewModel
import art.yniyniyni.freedomwave.ui.feature.bandwidth.TimeRange
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
                                            onEnable  = { vm.enableNode(node.uuid) },
                                            onDisable = { vm.disableNode(node.uuid) },
                                            onRestart = { vm.restartNode(node.uuid) }
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
private fun NodeListItem(
    node: Node,
    onClick: () -> Unit,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onRestart: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NodeStatusDot(node.status)
            Column(modifier = Modifier.weight(1f)) {
                Text(node.name, fontWeight = FontWeight.Medium)
                Text(
                    "${node.countryCode} · ${node.address}${node.port?.let { ":$it" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Traffic: ${formatBytes(node.trafficUsedBytes)}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun NodeStatusDot(status: NodeStatus) {
    val color = when (status) {
        NodeStatus.ONLINE     -> Color(0xFF4CAF50)
        NodeStatus.OFFLINE    -> MaterialTheme.colorScheme.error
        NodeStatus.DISABLED   -> MaterialTheme.colorScheme.onSurfaceVariant
        NodeStatus.CONNECTING -> Color(0xFFFF9800)
    }
    Surface(modifier = Modifier.size(12.dp), shape = androidx.compose.foundation.shape.CircleShape, color = color) {}
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Node Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        InfoRow("Status", node.status.name)
                        InfoRow("Address", "${node.address}${node.port?.let { ":$it" } ?: ""}")
                        InfoRow("Country", node.countryCode)
                        if (node.tags.isNotEmpty()) InfoRow("Tags", node.tags.joinToString(", "))
                        node.lastStatusMessage?.let { InfoRow("Message", it) }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Traffic", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        InfoRow("Used", formatBytes(node.trafficUsedBytes))
                        InfoRow("Limit", node.trafficLimitBytes?.let { formatBytes(it) } ?: "Unlimited")
                    }
                }
            }

            // Bandwidth chart card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Traffic History", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            TimeRange.entries.forEachIndexed { index, range ->
                                SegmentedButton(
                                    selected = bandwidthState.selectedRange == range,
                                    onClick = { onRangeChange(range) },
                                    shape = SegmentedButtonDefaults.itemShape(index, TimeRange.entries.size),
                                    label = { Text(range.label) }
                                )
                            }
                        }

                        when {
                            bandwidthState.isLoading ->
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))

                            nodeSeries.isEmpty() && bandwidthState.data != null ->
                                Text(
                                    "No traffic data for this node",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                            nodeSeries.isNotEmpty() ->
                                BandwidthChart(
                                    categories = bandwidthState.data?.categories ?: emptyList(),
                                    series = nodeSeries
                                )
                        }
                    }
                }
            }

            node.hostname?.let { hostname ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("System", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            InfoRow("Hostname", hostname)
                            node.cpus?.let { InfoRow("CPUs", it.toString()) }
                            node.memoryUsedBytes?.let { used ->
                                node.memoryTotalBytes?.let { total ->
                                    InfoRow("RAM", "${formatBytes(used)} / ${formatBytes(total)}")
                                }
                            }
                            node.uptimeSeconds?.let { InfoRow("Uptime", formatUptime(it)) }
                            node.loadAvg?.let { InfoRow("Load", "%.2f".format(it)) }
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (node.isOnline) {
                        Button(
                            onClick = onDisable,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) { Text("Disable Node") }
                        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Restart Node") }
                    } else if (node.isDisabled) {
                        Button(onClick = onEnable, modifier = Modifier.fillMaxWidth()) { Text("Enable Node") }
                    } else {
                        Button(onClick = onEnable, modifier = Modifier.fillMaxWidth()) { Text("Enable Node") }
                        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Restart Node") }
                    }
                    Button(
                        onClick = onReset,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) { Text("Reset Traffic") }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
