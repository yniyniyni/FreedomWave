package org.freedomwave.ui.feature.metrics

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_refresh
import freedomwave.composeapp.generated.resources.common_retry
import freedomwave.composeapp.generated.resources.metrics_cpu_cores
import freedomwave.composeapp.generated.resources.metrics_download
import freedomwave.composeapp.generated.resources.metrics_inbounds
import freedomwave.composeapp.generated.resources.metrics_no_tags
import freedomwave.composeapp.generated.resources.metrics_no_traffic
import freedomwave.composeapp.generated.resources.metrics_node_metrics
import freedomwave.composeapp.generated.resources.metrics_outbounds
import freedomwave.composeapp.generated.resources.metrics_rate
import freedomwave.composeapp.generated.resources.metrics_realtime
import freedomwave.composeapp.generated.resources.metrics_realtime_empty
import freedomwave.composeapp.generated.resources.metrics_rx
import freedomwave.composeapp.generated.resources.metrics_title
import freedomwave.composeapp.generated.resources.metrics_top_nodes
import freedomwave.composeapp.generated.resources.metrics_traffic
import freedomwave.composeapp.generated.resources.metrics_traffic_total
import freedomwave.composeapp.generated.resources.metrics_tx
import freedomwave.composeapp.generated.resources.metrics_upload
import freedomwave.composeapp.generated.resources.metrics_users_online
import freedomwave.composeapp.generated.resources.time_ago_minutes
import freedomwave.composeapp.generated.resources.time_ago_seconds
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.freedomwave.data.api.dto.BandwidthNodeSummaryDto
import org.freedomwave.domain.model.Node
import org.freedomwave.domain.model.NodeMetrics
import org.freedomwave.domain.model.NodeStatus
import org.freedomwave.domain.model.TagTraffic
import org.freedomwave.ui.components.BandwidthChart
import org.freedomwave.ui.components.ChartSeries
import org.freedomwave.ui.components.DetailSectionTitle
import org.freedomwave.ui.components.FwDetailCard
import org.freedomwave.ui.components.FwDetailTopBar
import org.freedomwave.ui.components.WaveLoader
import org.freedomwave.ui.feature.bandwidth.TimeRange
import org.freedomwave.ui.feature.bandwidth.label
import org.freedomwave.ui.l10n.localizedBytes
import org.freedomwave.ui.l10n.resolve
import org.freedomwave.ui.theme.LocalFwMonoFont
import org.freedomwave.ui.theme.LocalFwStatus
import org.freedomwave.util.countryFlag
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/** How often the realtime card re-reads node interface rates while the screen is visible. */
private const val LIVE_POLL_MS = 5_000L

@Composable
fun MetricsScreen(vm: MetricsViewModel = koinViewModel(), onBack: () -> Unit) {
    val state by vm.state.collectAsState()

    // Poll only while this screen is composed — leaving it cancels the loop.
    LaunchedEffect(Unit) {
        while (true) {
            delay(LIVE_POLL_MS)
            vm.refreshLive()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            FwDetailTopBar(
                title   = stringResource(Res.string.metrics_title),
                onBack  = onBack,
                actions = {
                    IconButton(onClick = vm::load) {
                        Icon(Icons.Rounded.Refresh, contentDescription = stringResource(Res.string.common_refresh))
                    }
                },
            )
        },
    ) { padding ->
        val nothingLoaded = state.nodes.isEmpty() && state.nodeMetrics.isEmpty() && state.bandwidth == null
        when {
            state.isLoading && nothingLoaded ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { WaveLoader() }

            state.error != null && nothingLoaded ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        state.error!!.resolve(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    TextButton(onClick = vm::load) { Text(stringResource(Res.string.common_retry)) }
                }

            else -> MetricsContent(state, vm::setRange, Modifier.padding(padding))
        }
    }
}

@Composable
private fun MetricsContent(state: MetricsUiState, onRangeChange: (TimeRange) -> Unit, modifier: Modifier) {
    val nodesByUuid = remember(state.nodes) { state.nodes.associateBy { it.uuid } }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "range") { RangeSelector(state.selectedRange, onRangeChange) }
        item(key = "realtime") { RealtimeCard(state.nodes, state.liveUpdatedAt) }
        item(key = "traffic") { TrafficCard(state) }

        val topNodes = state.bandwidth?.topNodes.orEmpty().filter { it.total > 0 }
        if (topNodes.isNotEmpty()) {
            item(key = "top") { TopNodesCard(topNodes) }
        }

        if (state.nodeMetrics.isNotEmpty()) {
            item(key = "metrics-header") {
                Box(Modifier.padding(top = 4.dp, start = 4.dp)) {
                    DetailSectionTitle(
                        stringResource(Res.string.metrics_node_metrics),
                        Icons.Rounded.Insights,
                        MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            items(state.nodeMetrics, key = { it.nodeUuid }) { metrics ->
                NodeMetricsCard(metrics, nodesByUuid[metrics.nodeUuid])
            }
        }
    }
}

@Composable
private fun RangeSelector(selected: TimeRange, onRangeChange: (TimeRange) -> Unit) {
    val ranges = remember { TimeRange.entries }
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ranges.forEachIndexed { index, range ->
            SegmentedButton(
                selected = selected == range,
                onClick  = { onRangeChange(range) },
                shape    = SegmentedButtonDefaults.itemShape(index, ranges.size),
                label    = { Text(range.label()) },
            )
        }
    }
}

// ── Realtime ────────────────────────────────────────────────────────────────

@Composable
private fun RealtimeCard(nodes: List<Node>, updatedAt: Instant?) {
    val live = remember(nodes) {
        nodes.filter { it.isOnline && it.rxBytesPerSec != null && it.txBytesPerSec != null }
            .sortedByDescending { (it.rxBytesPerSec ?: 0L) + (it.txBytesPerSec ?: 0L) }
    }
    FwDetailCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                DetailSectionTitle(stringResource(Res.string.metrics_realtime), Icons.Rounded.Speed)
            }
            if (updatedAt != null) UpdatedAgo(updatedAt)
        }
        if (live.isEmpty()) {
            Text(
                stringResource(Res.string.metrics_realtime_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            live.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { node -> RealtimeTile(node, Modifier.weight(1f)) }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun UpdatedAgo(at: Instant) {
    var now by remember { mutableStateOf(Clock.System.now()) }
    LaunchedEffect(at) {
        while (true) {
            now = Clock.System.now()
            delay(1_000)
        }
    }
    val seconds = (now - at).inWholeSeconds.coerceAtLeast(0)
    val text = if (seconds < 60) stringResource(Res.string.time_ago_seconds, seconds.toInt())
               else stringResource(Res.string.time_ago_minutes, (seconds / 60).toInt())
    Text(
        text,
        style = MaterialTheme.typography.labelMedium.copy(fontFamily = LocalFwMonoFont.current),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun RealtimeTile(node: Node, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StatusDot(node.status, 8.dp)
            Text(
                listOf(node.name, countryFlag(node.countryCode)).filter { it.isNotEmpty() }.joinToString(" "),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        RateRow(
            Icons.Rounded.ArrowDownward,
            stringResource(Res.string.metrics_rx),
            node.rxBytesPerSec ?: 0L,
            MaterialTheme.colorScheme.primary,
        )
        RateRow(
            Icons.Rounded.ArrowUpward,
            stringResource(Res.string.metrics_tx),
            node.txBytesPerSec ?: 0L,
            MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun RateRow(icon: ImageVector, label: String, bytesPerSec: Long, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            stringResource(Res.string.metrics_rate, localizedBytes(bytesPerSec)),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = LocalFwMonoFont.current),
            fontWeight = FontWeight.Medium,
            color = tint,
            maxLines = 1,
        )
    }
}

// ── Traffic chart ───────────────────────────────────────────────────────────

@Composable
private fun TrafficCard(state: MetricsUiState) {
    val data = state.bandwidth
    val total = data?.sparklineData?.sum() ?: 0.0
    val tint = MaterialTheme.colorScheme.primary
    FwDetailCard {
        DetailSectionTitle(
            stringResource(Res.string.metrics_traffic, state.selectedRange.label()),
            Icons.Rounded.ShowChart,
            MaterialTheme.colorScheme.tertiary,
        )
        when {
            state.isBandwidthLoading && data == null ->
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { WaveLoader() }

            data == null || total <= 0.0 ->
                Text(
                    stringResource(Res.string.metrics_no_traffic),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

            else -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Text(
                        stringResource(Res.string.metrics_traffic_total),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        localizedBytes(total.toLong()),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = LocalFwMonoFont.current),
                    )
                }
                BandwidthChart(
                    categories = data.categories,
                    series     = listOf(ChartSeries(name = "", color = "", data = data.sparklineData, tint = tint)),
                    modifier   = Modifier.fillMaxWidth().height(180.dp),
                    fillArea   = true,
                )
            }
        }
    }
}

// ── Top nodes ───────────────────────────────────────────────────────────────

@Composable
private fun TopNodesCard(topNodes: List<BandwidthNodeSummaryDto>) {
    val mono = LocalFwMonoFont.current
    val max = topNodes.maxOf { it.total }
    val primary = MaterialTheme.colorScheme.primary
    FwDetailCard {
        DetailSectionTitle(stringResource(Res.string.metrics_top_nodes), Icons.Rounded.Leaderboard)
        topNodes.forEachIndexed { index, node ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "#${index + 1}",
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                    )
                    Text(
                        listOf(node.name, countryFlag(node.countryCode)).filter { it.isNotEmpty() }.joinToString(" "),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        localizedBytes(node.total.toLong()),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = mono),
                        fontWeight = FontWeight.Medium,
                        color = primary,
                    )
                }
                Box(
                    Modifier.padding(start = 36.dp).fillMaxWidth().height(4.dp)
                        .clip(RoundedCornerShape(50)).background(primary.copy(alpha = 0.12f)),
                ) {
                    Box(
                        Modifier.fillMaxWidth((node.total / max).toFloat().coerceIn(0.02f, 1f)).fillMaxHeight()
                            .clip(RoundedCornerShape(50)).background(primary),
                    )
                }
            }
        }
    }
}

// ── Per-node metrics ────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NodeMetricsCard(metrics: NodeMetrics, node: Node?) {
    var expanded by rememberSaveable(metrics.nodeUuid) { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.fillMaxWidth().animateContentSize()) {
            Column(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (node != null) {
                        StatusDot(node.status, 10.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            listOf(metrics.nodeName, metrics.countryEmoji).filter { it.isNotBlank() }.joinToString(" "),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (metrics.providerName.isNotBlank()) {
                            Text(
                                metrics.providerName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    UsersOnlineBadge(metrics.usersOnline)
                    Icon(
                        Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp).rotate(rotation),
                    )
                }
                if (node != null) NodeResourceChips(node)
            }
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    TagSection(stringResource(Res.string.metrics_inbounds), metrics.inbounds)
                    TagSection(stringResource(Res.string.metrics_outbounds), metrics.outbounds)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NodeResourceChips(node: Node) {
    val fwStatus = LocalFwStatus.current
    val limit = node.trafficLimitBytes?.takeIf { it > 0 }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        node.cpus?.let { ResourceChip(Icons.Rounded.DeveloperBoard, stringResource(Res.string.metrics_cpu_cores, it)) }
        node.memoryUsedBytes?.let { used ->
            val total = node.memoryTotalBytes
            ResourceChip(
                Icons.Rounded.Memory,
                if (total != null && total > 0) "${localizedBytes(used)} / ${localizedBytes(total)}" else localizedBytes(used),
            )
        }
        if (limit != null) {
            val pct = (node.trafficUsedBytes.toFloat() / limit * 100f).coerceIn(0f, 100f)
            ResourceChip(
                Icons.Rounded.DataUsage,
                "${pct.roundToInt()}% · ${localizedBytes(node.trafficUsedBytes)} / ${localizedBytes(limit)}",
                tint = if (pct >= 90f) fwStatus.warning else fwStatus.online,
            )
        } else {
            ResourceChip(Icons.Rounded.DataUsage, localizedBytes(node.trafficUsedBytes))
        }
    }
}

@Composable
private fun ResourceChip(icon: ImageVector, text: String, tint: Color? = null) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(tint?.copy(alpha = 0.12f) ?: MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint ?: MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = LocalFwMonoFont.current),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TagSection(title: String, tags: List<TagTraffic>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (tags.isEmpty()) {
            Text(
                stringResource(Res.string.metrics_no_tags),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        tags.forEach { TagRow(it) }
    }
}

@Composable
private fun TagRow(tag: TagTraffic) {
    val mono = LocalFwMonoFont.current
    val idle = tag.uploadBytes == 0L && tag.downloadBytes == 0L
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // The pill wraps the tag but ellipsizes before it can push the values off the row.
        Box(Modifier.weight(1f)) {
            Text(
                tag.tag,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono),
                color = if (idle) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            TagValue(
                Icons.Rounded.ArrowUpward,
                stringResource(Res.string.metrics_upload),
                tag.uploadBytes?.let { localizedBytes(it) } ?: tag.uploadRaw,
                MaterialTheme.colorScheme.tertiary,
            )
            TagValue(
                Icons.Rounded.ArrowDownward,
                stringResource(Res.string.metrics_download),
                tag.downloadBytes?.let { localizedBytes(it) } ?: tag.downloadRaw,
                MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun TagValue(icon: ImageVector, description: String, value: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(12.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = LocalFwMonoFont.current),
            fontWeight = FontWeight.Medium,
            color = tint,
        )
    }
}

@Composable
private fun UsersOnlineBadge(count: Int) {
    val fwStatus = LocalFwStatus.current
    val active = count > 0
    val tint = if (active) fwStatus.online else MaterialTheme.colorScheme.onSurfaceVariant
    val description = stringResource(Res.string.metrics_users_online)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) fwStatus.online.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Rounded.Person, contentDescription = description, tint = tint, modifier = Modifier.size(14.dp))
        Text(
            count.toString(),
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = LocalFwMonoFont.current),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StatusDot(status: NodeStatus, size: Dp) {
    val fwStatus = LocalFwStatus.current
    val color = when (status) {
        NodeStatus.ONLINE     -> fwStatus.online
        NodeStatus.OFFLINE    -> fwStatus.offline
        NodeStatus.DISABLED   -> fwStatus.neutral
        NodeStatus.CONNECTING -> fwStatus.warning
    }
    Box(Modifier.size(size).clip(CircleShape).background(color))
}
