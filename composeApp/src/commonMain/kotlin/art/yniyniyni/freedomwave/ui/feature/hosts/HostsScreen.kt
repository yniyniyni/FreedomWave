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
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.common_cancel
import art.yniyniyni.freedomwave.resources.common_delete
import art.yniyniyni.freedomwave.resources.common_retry
import art.yniyniyni.freedomwave.resources.hosts_delete_confirm
import art.yniyniyni.freedomwave.resources.hosts_delete_title
import art.yniyniyni.freedomwave.resources.hosts_detail_address
import art.yniyniyni.freedomwave.resources.hosts_detail_allowed
import art.yniyniyni.freedomwave.resources.hosts_detail_alpn
import art.yniyniyni.freedomwave.resources.hosts_detail_connection
import art.yniyniyni.freedomwave.resources.hosts_detail_description
import art.yniyniyni.freedomwave.resources.hosts_detail_details
import art.yniyniyni.freedomwave.resources.hosts_detail_fingerprint
import art.yniyniyni.freedomwave.resources.hosts_detail_hidden
import art.yniyniyni.freedomwave.resources.hosts_detail_host_header
import art.yniyniyni.freedomwave.resources.hosts_detail_insecure_tls
import art.yniyniyni.freedomwave.resources.hosts_detail_nodes
import art.yniyniyni.freedomwave.resources.hosts_detail_path
import art.yniyniyni.freedomwave.resources.hosts_detail_security
import art.yniyniyni.freedomwave.resources.hosts_detail_shuffle
import art.yniyniyni.freedomwave.resources.hosts_detail_sni
import art.yniyniyni.freedomwave.resources.hosts_detail_status
import art.yniyniyni.freedomwave.resources.hosts_detail_tag
import art.yniyniyni.freedomwave.resources.hosts_detail_visibility
import art.yniyniyni.freedomwave.resources.hosts_detail_yes
import art.yniyniyni.freedomwave.resources.hosts_disable
import art.yniyniyni.freedomwave.resources.hosts_empty
import art.yniyniyni.freedomwave.resources.hosts_enable
import art.yniyniyni.freedomwave.resources.hosts_refresh
import art.yniyniyni.freedomwave.resources.hosts_status_disabled
import art.yniyniyni.freedomwave.resources.hosts_status_enabled
import art.yniyniyni.freedomwave.resources.hosts_title_count
import art.yniyniyni.freedomwave.ui.components.ShimmerList
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostsScreen(vm: HostsViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    val actionErrorText = state.actionError?.resolve()
    LaunchedEffect(actionErrorText) {
        actionErrorText?.let {
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
                        title   = stringResource(Res.string.hosts_title_count, state.hosts.size),
                        actions = { TextButton(onClick = vm::load) { Text(stringResource(Res.string.hosts_refresh)) } },
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
                                Text(state.error!!.resolve(), color = MaterialTheme.colorScheme.error)
                                Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) { Text(stringResource(Res.string.common_retry)) }
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
                                    stringResource(Res.string.hosts_empty),
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
                        FwChip(stringResource(Res.string.hosts_status_disabled).uppercase())
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
            title = { Text(stringResource(Res.string.hosts_delete_title)) },
            text  = { Text(stringResource(Res.string.hosts_delete_confirm, host.remark)) },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDelete() },
                    shape   = RoundedCornerShape(percent = 50),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(Res.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(Res.string.common_cancel)) }
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
                    DetailSectionTitle(stringResource(Res.string.hosts_detail_connection))
                    DetailRow(stringResource(Res.string.hosts_detail_address), "${host.address}:${host.port}", monoFont)
                    DetailRow(stringResource(Res.string.hosts_detail_security), host.securityLayer, monoFont)
                    host.sni?.let { DetailRow(stringResource(Res.string.hosts_detail_sni), it, monoFont) }
                    host.path?.let { DetailRow(stringResource(Res.string.hosts_detail_path), it, monoFont) }
                    host.host?.let { DetailRow(stringResource(Res.string.hosts_detail_host_header), it, monoFont) }
                    host.alpn?.let { DetailRow(stringResource(Res.string.hosts_detail_alpn), it, monoFont) }
                    host.fingerprint?.let { DetailRow(stringResource(Res.string.hosts_detail_fingerprint), it, monoFont) }
                    if (host.allowInsecure) DetailRow(
                        stringResource(Res.string.hosts_detail_insecure_tls),
                        stringResource(Res.string.hosts_detail_allowed),
                        monoFont,
                    )
                }
            }

            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.hosts_detail_details))
                    DetailRow(
                        stringResource(Res.string.hosts_detail_status),
                        stringResource(
                            if (host.isDisabled) Res.string.hosts_status_disabled else Res.string.hosts_status_enabled
                        ),
                        monoFont,
                    )
                    host.tag?.let { DetailRow(stringResource(Res.string.hosts_detail_tag), it, monoFont) }
                    host.serverDescription?.let { DetailRow(stringResource(Res.string.hosts_detail_description), it, monoFont) }
                    if (host.isHidden) DetailRow(
                        stringResource(Res.string.hosts_detail_visibility),
                        stringResource(Res.string.hosts_detail_hidden),
                        monoFont,
                    )
                    if (host.shuffleHost) DetailRow(
                        stringResource(Res.string.hosts_detail_shuffle),
                        stringResource(Res.string.hosts_detail_yes),
                        monoFont,
                    )
                    if (host.nodes.isNotEmpty()) DetailRow(stringResource(Res.string.hosts_detail_nodes), "${host.nodes.size}", monoFont)
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
                        Text(
                            stringResource(
                                if (host.isDisabled) Res.string.hosts_enable else Res.string.hosts_disable
                            )
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick  = { showDeleteDialog = true },
                        enabled  = !actionInProgress,
                        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(percent = 50),
                    ) {
                        Text(stringResource(Res.string.common_delete))
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
