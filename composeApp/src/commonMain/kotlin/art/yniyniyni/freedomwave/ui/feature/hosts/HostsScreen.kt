@file:OptIn(ExperimentalTransitionApi::class)

package art.yniyniyni.freedomwave.ui.feature.hosts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
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
import art.yniyniyni.freedomwave.ui.navigation.BackGestureEffect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import art.yniyniyni.freedomwave.domain.model.Host
import art.yniyniyni.freedomwave.ui.components.ShimmerList
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostsScreen(vm: HostsViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.actionError) {
        state.actionError?.let {
            snackbar.showSnackbar(it)
            vm.clearActionError()
        }
    }

    val isDetail = state.selected != null

    val transitionState = remember { SeekableTransitionState(false) }
    val transition = rememberTransition(transitionState, label = "hosts_nav")

    LaunchedEffect(isDetail) { transitionState.animateTo(isDetail) }

    // Snapshot the last selected host so the detail content stays alive during the exit animation.
    var lastSelectedHost by remember { mutableStateOf<Host?>(null) }
    val currentSelected = state.selected
    if (currentSelected != null) lastSelectedHost = currentSelected

    BackGestureEffect(
        enabled    = isDetail,
        onProgress = { fraction -> transitionState.seekTo(fraction, false) },
        onCommit   = { transitionState.animateTo(false); vm.clearSelection() },
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
            lastSelectedHost?.let { host ->
                HostDetailScreen(
                    host             = host,
                    actionInProgress = state.actionInProgress,
                    onBack           = vm::clearSelection,
                    onToggleEnabled  = { vm.toggleEnabled(host) },
                    onDelete         = { vm.delete(host) },
                )
            }
        } else {
            Scaffold(
                contentWindowInsets = WindowInsets(0),
                topBar = {
                    FwTopBar(
                        title   = "Hosts (${state.hosts.size})",
                        actions = { TextButton(onClick = vm::load) { Text("Refresh") } },
                    )
                },
                snackbarHost = { SnackbarHost(snackbar) },
            ) { padding ->
                when {
                    state.isLoading && state.hosts.isEmpty() ->
                        ShimmerList(modifier = Modifier.padding(padding))

                    state.error != null && state.hosts.isEmpty() ->
                        Box(Modifier.fillMaxSize().padding(padding)) {
                            Column(
                                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(state.error!!, color = MaterialTheme.colorScheme.error)
                                Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
                            }
                        }

                    else ->
                        PullToRefreshBox(
                            isRefreshing = state.isLoading && state.hosts.isNotEmpty(),
                            onRefresh    = vm::load,
                            modifier     = Modifier.fillMaxSize().padding(padding),
                        ) {
                            if (state.hosts.isEmpty()) {
                                Text(
                                    "No hosts",
                                    modifier = Modifier.align(Alignment.Center),
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                LazyColumn(
                                    contentPadding      = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    items(state.hosts, key = { it.uuid }) { host ->
                                        HostItem(host = host, onClick = { vm.select(host) })
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
private fun HostItem(host: Host, onClick: () -> Unit) {
    val monoFont = LocalFwMonoFont.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(host.remark, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${host.address}:${host.port}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FwChip(host.securityLayer)
                    host.tag?.let { FwTagChip(it) }
                    if (host.isDisabled) {
                        FwChip("DISABLED")
                    }
                }
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun FwChip(label: String) {
    SuggestionChip(
        onClick = {},
        label   = { Text(label, style = MaterialTheme.typography.labelSmall) },
        shape   = RoundedCornerShape(percent = 50),
        colors  = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            labelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border  = null,
    )
}

@Composable
private fun FwTagChip(label: String) {
    SuggestionChip(
        onClick = {},
        label   = { Text(label, style = MaterialTheme.typography.labelSmall) },
        shape   = RoundedCornerShape(percent = 50),
        colors  = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            labelColor     = MaterialTheme.colorScheme.tertiary,
        ),
        border  = null,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HostDetailScreen(
    host: Host,
    actionInProgress: Boolean,
    onBack: () -> Unit,
    onToggleEnabled: () -> Unit,
    onDelete: () -> Unit
) {
    val monoFont = LocalFwMonoFont.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete host") },
            text  = { Text("Delete \"${host.remark}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDelete() },
                    shape   = RoundedCornerShape(percent = 50),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { FwDetailTopBar(title = host.remark, onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FwDetailCard {
                    DetailSectionTitle("Connection")
                    DetailRow("Address", "${host.address}:${host.port}", monoFont)
                    DetailRow("Security", host.securityLayer, monoFont)
                    host.sni?.let { DetailRow("SNI", it, monoFont) }
                    host.path?.let { DetailRow("Path", it, monoFont) }
                    host.host?.let { DetailRow("Host header", it, monoFont) }
                    host.alpn?.let { DetailRow("ALPN", it, monoFont) }
                    host.fingerprint?.let { DetailRow("Fingerprint", it, monoFont) }
                    if (host.allowInsecure) DetailRow("Insecure TLS", "Allowed", monoFont)
                }
            }

            item {
                FwDetailCard {
                    DetailSectionTitle("Details")
                    DetailRow("Status", if (host.isDisabled) "Disabled" else "Enabled", monoFont)
                    host.tag?.let { DetailRow("Tag", it, monoFont) }
                    host.serverDescription?.let { DetailRow("Description", it, monoFont) }
                    if (host.isHidden) DetailRow("Visibility", "Hidden", monoFont)
                    if (host.shuffleHost) DetailRow("Shuffle host", "Yes", monoFont)
                    if (host.nodes.isNotEmpty()) DetailRow("Nodes", "${host.nodes.size}", monoFont)
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick  = onToggleEnabled,
                        enabled  = !actionInProgress,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(percent = 50),
                    ) {
                        Text(if (host.isDisabled) "Enable" else "Disable")
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick  = { showDeleteDialog = true },
                        enabled  = !actionInProgress,
                        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(percent = 50),
                    ) {
                        Text("Delete")
                    }
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
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
        )
    }
}
