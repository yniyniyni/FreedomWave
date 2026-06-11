package art.yniyniyni.freedomwave.ui.feature.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import art.yniyniyni.freedomwave.domain.model.DashboardStats
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.common_empty_dash
import art.yniyniyni.freedomwave.resources.common_retry
import art.yniyniyni.freedomwave.resources.dashboard_countries
import art.yniyniyni.freedomwave.resources.dashboard_cpu_cores
import art.yniyniyni.freedomwave.resources.dashboard_last_24h
import art.yniyniyni.freedomwave.resources.dashboard_last_7d
import art.yniyniyni.freedomwave.resources.dashboard_lifetime_traffic
import art.yniyniyni.freedomwave.resources.dashboard_live
import art.yniyniyni.freedomwave.resources.dashboard_never
import art.yniyniyni.freedomwave.resources.dashboard_nodes
import art.yniyniyni.freedomwave.resources.dashboard_online
import art.yniyniyni.freedomwave.resources.dashboard_online_activity
import art.yniyniyni.freedomwave.resources.dashboard_online_nodes
import art.yniyniyni.freedomwave.resources.dashboard_online_users
import art.yniyniyni.freedomwave.resources.dashboard_panel
import art.yniyniyni.freedomwave.resources.dashboard_ram
import art.yniyniyni.freedomwave.resources.dashboard_refresh
import art.yniyniyni.freedomwave.resources.dashboard_status_active
import art.yniyniyni.freedomwave.resources.dashboard_status_disabled
import art.yniyniyni.freedomwave.resources.dashboard_status_expired
import art.yniyniyni.freedomwave.resources.dashboard_status_limited
import art.yniyniyni.freedomwave.resources.dashboard_system
import art.yniyniyni.freedomwave.resources.dashboard_this_month
import art.yniyniyni.freedomwave.resources.dashboard_title
import art.yniyniyni.freedomwave.resources.dashboard_today
import art.yniyniyni.freedomwave.resources.dashboard_total
import art.yniyniyni.freedomwave.resources.dashboard_total_count
import art.yniyniyni.freedomwave.resources.dashboard_traffic
import art.yniyniyni.freedomwave.resources.dashboard_uptime
import art.yniyniyni.freedomwave.resources.dashboard_users_by_status
import art.yniyniyni.freedomwave.resources.dashboard_version
import art.yniyniyni.freedomwave.resources.time_ago_hours
import art.yniyniyni.freedomwave.resources.time_ago_minutes
import art.yniyniyni.freedomwave.resources.time_ago_seconds
import art.yniyniyni.freedomwave.ui.components.FwTopBar
import art.yniyniyni.freedomwave.ui.components.SparklineChart
import art.yniyniyni.freedomwave.ui.feature.bandwidth.BandwidthViewModel
import art.yniyniyni.freedomwave.ui.l10n.UiText
import art.yniyniyni.freedomwave.ui.l10n.localized
import art.yniyniyni.freedomwave.ui.l10n.localizedBytes
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import art.yniyniyni.freedomwave.ui.theme.LocalFwStatus
import art.yniyniyni.freedomwave.util.uptimeParts
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    vm: DashboardViewModel = koinViewModel(),
    bandwidthVm: BandwidthViewModel = koinViewModel()
) {
    val state by vm.state.collectAsState()
    val bandwidthState by bandwidthVm.state.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            FwTopBar(
                title = stringResource(Res.string.dashboard_title),
                actions = { TextButton(onClick = vm::refresh) { Text(stringResource(Res.string.dashboard_refresh)) } },
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
                    PullToRefreshBox(
                        isRefreshing = state.isLoading,
                        onRefresh = vm::refresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        StatsList(
                            stats = state.stats!!,
                            sparklineData = bandwidthState.data?.sparklineData ?: emptyList(),
                            sparklineCategories = bandwidthState.data?.categories ?: emptyList(),
                            lastUpdatedAt = state.lastUpdatedAt
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
    sparklineCategories: List<String>,
    lastUpdatedAt: Long?
) {
    // 1s ticker for freshness pill
    val now by produceState(initialValue = lastUpdatedAt ?: 0L, lastUpdatedAt) {
        while (true) {
            value = Clock.System.now().toEpochMilliseconds()
            delay(1000)
        }
    }
    val secs: Long? = if (lastUpdatedAt == null) null else ((now - lastUpdatedAt) / 1000).coerceAtLeast(0)
    val agoString: String = when {
        secs == null     -> stringResource(Res.string.common_empty_dash)
        secs < 60        -> stringResource(Res.string.time_ago_seconds, secs)
        secs < 3600      -> stringResource(Res.string.time_ago_minutes, secs / 60)
        else             -> stringResource(Res.string.time_ago_hours, secs / 3600)
    }
    val isFresh = secs != null && secs < 60

    val fwStatus = LocalFwStatus.current
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 1. Live header + 2×2 tile grid ────────────────────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: bolt icon + "Live" label
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = stringResource(Res.string.dashboard_live),
                            style = MaterialTheme.typography.titleMedium,
                            color = onSurface
                        )
                    }
                    // Right: freshness pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isFresh) fwStatus.online else fwStatus.neutral)
                            )
                            Text(
                                text = agoString,
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurfaceVariant
                            )
                        }
                    }
                }

                // Row A: Online Users | Online Nodes
                val todayDelta = stats.todayDeltaPercent

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiveTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Person,
                        tint = fwStatus.online,
                        label = stringResource(Res.string.dashboard_online_users).uppercase(),
                        value = stats.onlineNow.toString()
                    )
                    LiveTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Hub,
                        tint = fwStatus.warning,
                        label = stringResource(Res.string.dashboard_online_nodes).uppercase(),
                        value = "${stats.onlineNodes} / ${stats.totalNodes}"
                    )
                }

                // Row B: Today | This Month
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiveTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Bolt,
                        tint = primary,
                        label = stringResource(Res.string.dashboard_today).uppercase(),
                        value = localizedBytes(stats.todayBytes),
                        badge = if (todayDelta != null) {
                            {
                                val deltaColor = if (todayDelta < 0) fwStatus.offline else fwStatus.online
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = deltaColor.copy(alpha = 0.16f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (todayDelta < 0) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                                            contentDescription = null,
                                            tint = deltaColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "${abs(todayDelta)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = deltaColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        } else null
                    )
                    LiveTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.CalendarMonth,
                        tint = tertiary,
                        label = stringResource(Res.string.dashboard_this_month).uppercase(),
                        value = localizedBytes(stats.monthTraffic)
                    )
                }
            }
        }

        // ── 2. Online Activity card ────────────────────────────────────────────
        item {
            FwCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionTitle(stringResource(Res.string.dashboard_online_activity), Icons.Rounded.BarChart)

                    // 3 mini activity tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActivityTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Schedule,
                            value = stats.onlineLastDay.toString(),
                            label = stringResource(Res.string.dashboard_last_24h).uppercase()
                        )
                        ActivityTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.CalendarMonth,
                            value = stats.onlineLastWeek.toString(),
                            label = stringResource(Res.string.dashboard_last_7d).uppercase()
                        )
                        ActivityTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Bedtime,
                            value = stats.neverOnline.toString(),
                            label = stringResource(Res.string.dashboard_never).uppercase()
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Users by Status
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(Res.string.dashboard_users_by_status).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = stringResource(Res.string.dashboard_total_count, stats.totalUsers),
                                style = MaterialTheme.typography.labelMedium,
                                color = onSurfaceVariant
                            )
                        }

                        // Fixed order: ACTIVE, LIMITED, EXPIRED (if present), DISABLED
                        val activeLabel   = stringResource(Res.string.dashboard_status_active)
                        val limitedLabel  = stringResource(Res.string.dashboard_status_limited)
                        val expiredLabel  = stringResource(Res.string.dashboard_status_expired)
                        val disabledLabel = stringResource(Res.string.dashboard_status_disabled)
                        val statusEntries = buildList {
                            add(Triple(activeLabel,   stats.statusCounts["ACTIVE"]   ?: 0, fwStatus.online))
                            add(Triple(limitedLabel,  stats.statusCounts["LIMITED"]  ?: 0, fwStatus.warning))
                            if (stats.statusCounts.containsKey("EXPIRED")) {
                                add(Triple(expiredLabel, stats.statusCounts["EXPIRED"] ?: 0, fwStatus.neutral))
                            }
                            add(Triple(disabledLabel, stats.statusCounts["DISABLED"] ?: 0, fwStatus.offline))
                        }
                        statusEntries.forEach { (label, count, color) ->
                            StatusBar(
                                label = label.uppercase(),
                                count = count,
                                total = stats.totalUsers,
                                color = color
                            )
                        }
                    }
                }
            }
        }

        // ── 3. Traffic sparkline (moved here, after activity card) ─────────────
        if (sparklineData.isNotEmpty()) {
            item {
                FwCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SectionTitle(stringResource(Res.string.dashboard_traffic), Icons.Rounded.Bolt)
                            if (sparklineCategories.isNotEmpty()) {
                                Text(
                                    "${sparklineCategories.first().takeLast(5)} – ${sparklineCategories.last().takeLast(5)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceVariant
                                )
                            }
                        }
                        SparklineChart(data = sparklineData)
                    }
                }
            }
        }

        // ── 4. Detail cards: Panel, Nodes, System ─────────────────────────────
        item {
            StatCard(title = stringResource(Res.string.dashboard_panel), icon = Icons.Rounded.Public) {
                StatRow(stringResource(Res.string.dashboard_version), stats.panelVersion)
                StatRow(stringResource(Res.string.dashboard_uptime), uptimeParts(stats.uptimeSeconds).localized())
                StatRow(stringResource(Res.string.dashboard_countries), stats.distinctCountries.toString())
            }
        }
        item {
            StatCard(title = stringResource(Res.string.dashboard_nodes), icon = Icons.Rounded.Dns) {
                StatRow(stringResource(Res.string.dashboard_online), stats.onlineNodes.toString())
                StatRow(stringResource(Res.string.dashboard_total), stats.totalNodes.toString())
                StatRow(stringResource(Res.string.dashboard_lifetime_traffic), localizedBytes(stats.nodesBytesLifetime))
            }
        }
        item {
            StatCard(title = stringResource(Res.string.dashboard_system), icon = Icons.Rounded.Computer) {
                StatRow(stringResource(Res.string.dashboard_cpu_cores), stats.cpuCores.toString())
                StatRow(stringResource(Res.string.dashboard_ram), "${localizedBytes(stats.memoryUsedBytes)} / ${localizedBytes(stats.memoryTotalBytes)}")
            }
        }
    }
}

// ── Private composables ────────────────────────────────────────────────────────

@Composable
private fun LiveTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String,
    badge: @Composable (() -> Unit)? = null
) {
    val monoFont = LocalFwMonoFont.current
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                badge?.invoke()
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = monoFont),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ActivityTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    val monoFont = LocalFwMonoFont.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = monoFont),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatusBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val monoFont = LocalFwMonoFont.current
    val fraction = if (total > 0) (count.toFloat() / total) else 0f
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.width(110.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = monoFont),
            color = color
        )
    }
}

@Composable
private fun FwCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = { content() })
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.padding(end = 2.dp),
        )
        Text(text = title, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun StatCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    FwCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(title, icon)
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    val monoFont = LocalFwMonoFont.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium.copy(fontFamily = monoFont),
            color      = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ErrorState(error: UiText, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(error.resolve(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) { Text(stringResource(Res.string.common_retry)) }
    }
}
