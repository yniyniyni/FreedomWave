package org.freedomwave.ui.feature.users

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Dns
import org.freedomwave.ui.components.WaveLoader
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.freedomwave.domain.model.User
import org.freedomwave.ui.components.BandwidthChart
import org.freedomwave.ui.components.ChartSeries
import org.freedomwave.ui.components.DetailRow
import org.freedomwave.ui.components.DetailSectionTitle
import org.freedomwave.ui.components.FwDetailCard
import org.freedomwave.ui.components.FwDetailTopBar
import org.freedomwave.ui.feature.bandwidth.TimeRange
import org.freedomwave.ui.feature.bandwidth.label
import org.freedomwave.ui.l10n.localizedBytes
import org.freedomwave.ui.l10n.resolve
import org.freedomwave.util.countryFlag
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_cancel
import freedomwave.composeapp.generated.resources.common_ok
import freedomwave.composeapp.generated.resources.common_retry
import freedomwave.composeapp.generated.resources.users_traffic_custom_range
import freedomwave.composeapp.generated.resources.users_traffic_no_data
import freedomwave.composeapp.generated.resources.users_traffic_pick_range
import freedomwave.composeapp.generated.resources.users_traffic_title
import freedomwave.composeapp.generated.resources.users_traffic_top_nodes
import freedomwave.composeapp.generated.resources.users_traffic_usage_trend
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTrafficStatsScreen(
    user: User,
    viewModel: UserTrafficViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showRangePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FwDetailTopBar(
                title = stringResource(Res.string.users_traffic_title, user.username),
                onBack = onBackClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading && state.data == null -> {
                    WaveLoader(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null && state.data == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.error!!.resolve(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextButton(onClick = { viewModel.load() }) {
                            Text(stringResource(Res.string.common_retry))
                        }
                    }
                }
                state.data != null -> {
                    val data = state.data!!
                    val chartSeries = remember(data) {
                        data.series.map { ChartSeries(it.name, it.color, it.data) }
                    }
                    val hasChart = data.categories.isNotEmpty() && chartSeries.any { it.data.isNotEmpty() }
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            FwDetailCard {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TimeRange.entries.forEach { range ->
                                            FilterChip(
                                                selected = state.selectedRange == range,
                                                onClick = { viewModel.setRange(range) },
                                                label = { Text(range.label()) }
                                            )
                                        }
                                        val customActive = state.selectedRange == null
                                        FilterChip(
                                            selected = customActive,
                                            onClick = { showRangePicker = true },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Rounded.DateRange,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    if (customActive && state.customStart != null && state.customEnd != null) {
                                                        "${state.customStart!!.takeLast(5)} – ${state.customEnd!!.takeLast(5)}"
                                                    } else {
                                                        stringResource(Res.string.users_traffic_custom_range)
                                                    }
                                                )
                                            }
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        DetailSectionTitle(stringResource(Res.string.users_traffic_usage_trend), Icons.Rounded.BarChart)
                                        if (data.categories.isNotEmpty()) {
                                            Text(
                                                "${data.categories.first().takeLast(5)} – ${data.categories.last().takeLast(5)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (hasChart) {
                                        BandwidthChart(
                                            categories = data.categories,
                                            series = chartSeries
                                        )
                                    } else {
                                        Text(
                                            stringResource(Res.string.users_traffic_no_data),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (data.topNodes.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                DetailSectionTitle(stringResource(Res.string.users_traffic_top_nodes), Icons.Rounded.Dns)
                            }
                            items(data.topNodes, key = { it.uuid }) { node ->
                                FwDetailCard {
                                    DetailRow(
                                        label = "${countryFlag(node.countryCode)} ${node.name}",
                                        value = localizedBytes(node.total.toLong())
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRangePicker) {
        val pickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedStartDateMillis != null &&
                        pickerState.selectedEndDateMillis != null,
                    onClick = {
                        val start = pickerState.selectedStartDateMillis
                        val end = pickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            viewModel.setCustomRange(millisToIsoDate(start), millisToIsoDate(end))
                        }
                        showRangePicker = false
                    }
                ) { Text(stringResource(Res.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) {
                    Text(stringResource(Res.string.common_cancel))
                }
            }
        ) {
            DateRangePicker(
                state = pickerState,
                title = {
                    Text(
                        stringResource(Res.string.users_traffic_pick_range),
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                    )
                }
            )
        }
    }
}

/** Material date pickers report the selected day at UTC midnight; format it as ISO `YYYY-MM-DD`. */
private fun millisToIsoDate(millis: Long): String =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date.toString()
