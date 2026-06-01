package art.yniyniyni.freedomwave.ui.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.domain.model.DashboardStats
import art.yniyniyni.freedomwave.ui.components.SparklineChart
import art.yniyniyni.freedomwave.ui.feature.bandwidth.BandwidthViewModel
import art.yniyniyni.freedomwave.util.formatBytesStr
import art.yniyniyni.freedomwave.util.formatUptime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    vm: DashboardViewModel = koinViewModel(),
    bandwidthVm: BandwidthViewModel = koinViewModel()
) {
    val state by vm.state.collectAsState()
    val bandwidthState by bandwidthVm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = { TextButton(onClick = vm::refresh) { Text("Refresh") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.stats == null ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                state.error != null && state.stats == null ->
                    ErrorState(error = state.error!!, onRetry = vm::refresh)

                state.stats != null -> {
                    val isRefreshing = state.isLoading
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = vm::refresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        StatsList(
                            stats = state.stats!!,
                            sparklineData = bandwidthState.data?.sparklineData ?: emptyList(),
                            sparklineCategories = bandwidthState.data?.categories ?: emptyList()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsList(
    stats: DashboardStats,
    sparklineData: List<Double>,
    sparklineCategories: List<String>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Traffic sparkline card
        if (sparklineData.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Traffic (${sparklineCategories.size}d)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            if (sparklineCategories.isNotEmpty()) {
                                Text(
                                    "${sparklineCategories.first().takeLast(5)} – ${sparklineCategories.last().takeLast(5)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        SparklineChart(data = sparklineData)
                    }
                }
            }
        }

        item {
            StatCard(title = "Panel") {
                StatRow("Version", stats.panelVersion)
                StatRow("Uptime", formatUptime(stats.uptimeSeconds))
                StatRow("Countries", stats.distinctCountries.toString())
            }
        }
        item {
            StatCard(title = "Users") {
                StatRow("Active", stats.activeUsers.toString())
                StatRow("Total", stats.totalUsers.toString())
                StatRow("Online now", stats.onlineNow.toString())
                StatRow("Last 24h", stats.onlineLastDay.toString())
                StatRow("Last 7d", stats.onlineLastWeek.toString())
            }
        }
        item {
            StatCard(title = "Nodes") {
                StatRow("Online", stats.onlineNodes.toString())
                StatRow("Total", stats.totalNodes.toString())
                StatRow("Lifetime traffic", formatBytesStr(stats.nodesBytesLifetime))
            }
        }
        item {
            StatCard(title = "Traffic") {
                StatRow("This month", formatBytesStr(stats.monthTraffic))
                StatRow("All time", formatBytesStr(stats.totalTraffic))
            }
        }
        item {
            StatCard(title = "System") {
                StatRow("CPU cores", stats.cpuCores.toString())
                val usedGb = stats.memoryUsedBytes / 1_073_741_824.0
                val totalGb = stats.memoryTotalBytes / 1_073_741_824.0
                StatRow("RAM", "%.1f / %.1f GB".format(usedGb, totalGb))
            }
        }
    }
}

@Composable
private fun StatCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
    }
}
