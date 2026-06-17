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
import androidx.compose.foundation.layout.size
import art.yniyniyni.freedomwave.ui.navigation.BackGestureEffect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.domain.model.Host
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_cancel
import freedomwave.composeapp.generated.resources.common_delete
import freedomwave.composeapp.generated.resources.common_retry
import freedomwave.composeapp.generated.resources.hosts_delete_confirm
import freedomwave.composeapp.generated.resources.hosts_delete_title
import freedomwave.composeapp.generated.resources.hosts_detail_address
import freedomwave.composeapp.generated.resources.hosts_detail_allowed
import freedomwave.composeapp.generated.resources.hosts_detail_alpn
import freedomwave.composeapp.generated.resources.hosts_detail_connection
import freedomwave.composeapp.generated.resources.hosts_detail_description
import freedomwave.composeapp.generated.resources.hosts_detail_details
import freedomwave.composeapp.generated.resources.hosts_detail_edit
import freedomwave.composeapp.generated.resources.hosts_detail_fingerprint
import freedomwave.composeapp.generated.resources.hosts_detail_hidden
import freedomwave.composeapp.generated.resources.hosts_detail_host_header
import freedomwave.composeapp.generated.resources.hosts_detail_inbound
import freedomwave.composeapp.generated.resources.hosts_detail_insecure_tls
import freedomwave.composeapp.generated.resources.hosts_detail_nodes
import freedomwave.composeapp.generated.resources.hosts_detail_path
import freedomwave.composeapp.generated.resources.hosts_detail_security
import freedomwave.composeapp.generated.resources.hosts_detail_shuffle
import freedomwave.composeapp.generated.resources.hosts_detail_sni
import freedomwave.composeapp.generated.resources.hosts_detail_status
import freedomwave.composeapp.generated.resources.hosts_detail_tag
import freedomwave.composeapp.generated.resources.hosts_detail_visibility
import freedomwave.composeapp.generated.resources.hosts_detail_vless_route
import freedomwave.composeapp.generated.resources.hosts_detail_xray_template
import freedomwave.composeapp.generated.resources.hosts_detail_yes
import freedomwave.composeapp.generated.resources.hosts_disable
import freedomwave.composeapp.generated.resources.hosts_empty
import freedomwave.composeapp.generated.resources.hosts_enable
import freedomwave.composeapp.generated.resources.hosts_form_add
import freedomwave.composeapp.generated.resources.common_refresh
import freedomwave.composeapp.generated.resources.hosts_status_disabled
import freedomwave.composeapp.generated.resources.hosts_status_enabled
import freedomwave.composeapp.generated.resources.hosts_title_count
import art.yniyniyni.freedomwave.ui.components.ShimmerList
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private sealed interface HostsNav {
    data object List : HostsNav
    data class Detail(val host: Host) : HostsNav
    data class Form(val host: Host?, val epoch: Int) : HostsNav

    val depth: Int get() = when (this) {
        List -> 0
        is Detail -> 1
        is Form -> if (host == null) 1 else 2
    }
    val key: String get() = when (this) {
        List -> "list"
        is Detail -> "detail:${host.uuid}"
        is Form -> "form:${host?.uuid ?: "new"}:$epoch"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostsScreen(vm: HostsViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var stack by remember { mutableStateOf<kotlin.collections.List<HostsNav>>(listOf(HostsNav.List)) }
    var formEpoch by remember { mutableStateOf(0) }
    val top = stack.last()
    val canGoBack = stack.size > 1

    val actionErrorText = state.actionError?.resolve()
    LaunchedEffect(actionErrorText) {
        actionErrorText?.let {
            snackbar.showSnackbar(it)
            vm.clearActionError()
        }
    }

    val transitionState = remember { SeekableTransitionState<HostsNav>(HostsNav.List) }
    val transition = rememberTransition(transitionState, label = "hosts_nav")
    LaunchedEffect(top) { if (transitionState.currentState != top) transitionState.animateTo(top) }

    BackGestureEffect(
        enabled    = canGoBack,
        onProgress = { fraction -> transitionState.seekTo(fraction, stack[stack.size - 2]) },
        onCommit   = { val t = stack[stack.size - 2]; transitionState.animateTo(t); stack = stack.dropLast(1) },
        onCancel   = { transitionState.animateTo(top) },
    )

    transition.AnimatedContent(
        contentKey = { it.key },
        transitionSpec = {
            val deeper = targetState.depth > initialState.depth
            if (deeper) {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 4 }
            } else {
                slideInHorizontally { -it / 4 } togetherWith slideOutHorizontally { it }
            }.apply { targetContentZIndex = if (deeper) 1f else 0f }
        },
    ) { navEntry ->
        when (navEntry) {
            is HostsNav.List -> HostsListContent(
                state    = state,
                vm       = vm,
                snackbar = snackbar,
                onOpenDetail = { host -> stack = stack + HostsNav.Detail(host) },
                onCreate     = { formEpoch++; stack = stack + HostsNav.Form(null, formEpoch) },
            )
            is HostsNav.Detail -> {
                val live = state.hosts.find { it.uuid == navEntry.host.uuid } ?: navEntry.host
                HostDetailScreen(
                    host             = live,
                    actionInProgress = state.actionInProgress,
                    onBack           = { stack = stack.dropLast(1) },
                    onEdit           = { formEpoch++; stack = stack + HostsNav.Form(live, formEpoch) },
                    onToggleEnabled  = { vm.toggleEnabled(live) },
                    onDelete         = { vm.delete(live); stack = stack.dropLast(1) },
                )
            }
            is HostsNav.Form -> {
                val uuid = navEntry.host?.uuid
                val formVm: HostFormViewModel = koinViewModel(key = "host-form-${navEntry.epoch}") { parametersOf(uuid) }
                HostCreateEditScreen(
                    vm     = formVm,
                    onBack = { stack = stack.dropLast(1) },
                    onSaved = {
                        vm.load()
                        if (stack.lastOrNull() is HostsNav.Form) {
                            stack = if (uuid == null) listOf(HostsNav.List) else stack.dropLast(1)
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HostsListContent(
    state: HostsUiState,
    vm: HostsViewModel,
    snackbar: SnackbarHostState,
    onOpenDetail: (Host) -> Unit,
    onCreate: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            FwTopBar(
                title   = stringResource(Res.string.hosts_title_count, state.hosts.size),
                actions = { IconButton(onClick = vm::load) { Icon(Icons.Rounded.Refresh, contentDescription = stringResource(Res.string.common_refresh)) } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation      = FloatingActionButtonDefaults.elevation(0.dp),
            ) { Icon(Icons.Rounded.Add, contentDescription = stringResource(Res.string.hosts_form_add)) }
        },
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
                                HostItem(host = host, onClick = { onOpenDetail(host) })
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
    onEdit: () -> Unit,
    onToggleEnabled: () -> Unit,
    onDelete: () -> Unit,
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
        topBar = {
            FwDetailTopBar(
                title   = host.remark,
                onBack  = onBack,
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = stringResource(Res.string.hosts_detail_edit))
                    }
                },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.hosts_detail_connection), Icons.Rounded.Link)
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
                    DetailSectionTitle(stringResource(Res.string.hosts_detail_details), Icons.Rounded.Tune)
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
                    host.configProfileInboundUuid?.let { DetailRow(stringResource(Res.string.hosts_detail_inbound), it, monoFont) }
                    host.vlessRouteId?.let { DetailRow(stringResource(Res.string.hosts_detail_vless_route), it.toString(), monoFont) }
                    host.xrayJsonTemplateUuid?.let { DetailRow(stringResource(Res.string.hosts_detail_xray_template), it, monoFont) }
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
