package org.freedomwave.ui.feature.hosts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.freedomwave.ui.components.FwNavDestination
import org.freedomwave.ui.theme.LocalFwStatus
import freedomwave.composeapp.generated.resources.hosts_status_enabled
import freedomwave.composeapp.generated.resources.hosts_status_hidden
import org.freedomwave.ui.components.FwNavigationContainer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import org.freedomwave.ui.components.FwTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.freedomwave.domain.model.Host
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_refresh
import freedomwave.composeapp.generated.resources.common_retry
import freedomwave.composeapp.generated.resources.hosts_empty
import freedomwave.composeapp.generated.resources.hosts_form_add
import freedomwave.composeapp.generated.resources.hosts_status_disabled
import freedomwave.composeapp.generated.resources.hosts_title_count
import org.freedomwave.ui.components.WaveLoader
import org.freedomwave.ui.l10n.resolve
import org.freedomwave.ui.theme.LocalFwMonoFont
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

enum class HostStatusKind { DISABLED, HIDDEN, ENABLED }

fun hostStatusKind(host: Host): HostStatusKind = when {
    host.isDisabled -> HostStatusKind.DISABLED
    host.isHidden   -> HostStatusKind.HIDDEN
    else            -> HostStatusKind.ENABLED
}

private sealed interface HostsNav : FwNavDestination {
    data object List : HostsNav
    data class Editor(val host: Host?, val epoch: Int) : HostsNav   // null = create

    override val depth: Int get() = if (this is Editor) 1 else 0
    override val key: String get() = when (this) {
        List -> "list"
        is Editor -> "editor:${host?.uuid ?: "new"}:$epoch"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostsScreen(vm: HostsViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()

    var formEpoch by remember { mutableStateOf(0) }

    FwNavigationContainer<HostsNav>(
        navLabel = "hosts_nav",
        rootState = HostsNav.List,
        initialStack = listOf(HostsNav.List),
        actionError = state.actionError,
        onClearActionError = vm::clearActionError,
        contentKey = { it.key },
    ) { navEntry, push, pop, currentStack, snackbarHost ->
        when (navEntry) {
            is HostsNav.List -> HostsListContent(
                state    = state,
                vm       = vm,
                snackbar = snackbarHost,
                onOpenHost = { host -> formEpoch++; push(HostsNav.Editor(host, formEpoch)) },
                onCreate   = { formEpoch++; push(HostsNav.Editor(null, formEpoch)) },
            )
            is HostsNav.Editor -> {
                val uuid = navEntry.host?.uuid
                val formVm: HostFormViewModel = koinViewModel(key = "host-form-${navEntry.epoch}") { parametersOf(uuid) }
                HostCreateEditScreen(
                    vm      = formVm,
                    onBack  = { pop() },
                    onSaved = {
                        vm.load()
                        if (currentStack().lastOrNull() is HostsNav.Editor) pop()
                    },
                    onDelete = navEntry.host?.let { host ->
                        {
                            vm.delete(host)
                            if (currentStack().lastOrNull() is HostsNav.Editor) pop()
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
    onOpenHost: (Host) -> Unit,
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
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { WaveLoader() }

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
                        val lazyListState = rememberLazyListState()
                        val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
                            vm.moveHost(from.index, to.index)
                        }
                        LazyColumn(
                            state               = lazyListState,
                            contentPadding      = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.hosts, key = { it.uuid }) { host ->
                                ReorderableItem(reorderState, key = host.uuid) { _ ->
                                    HostItem(
                                        host = host,
                                        onClick = { onOpenHost(host) },
                                        dragModifier = Modifier.longPressDraggableHandle(
                                            onDragStarted = { vm.beginReorder() },
                                            onDragStopped = { vm.commitReorder() },
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }
}

@Composable
private fun HostStatusDot(host: Host) {
    val fwStatus = LocalFwStatus.current
    val color = when (hostStatusKind(host)) {
        HostStatusKind.DISABLED -> fwStatus.neutral
        HostStatusKind.HIDDEN   -> fwStatus.hidden
        HostStatusKind.ENABLED  -> fwStatus.online
    }
    val desc = stringResource(
        when (hostStatusKind(host)) {
            HostStatusKind.DISABLED -> Res.string.hosts_status_disabled
            HostStatusKind.HIDDEN   -> Res.string.hosts_status_hidden
            HostStatusKind.ENABLED  -> Res.string.hosts_status_enabled
        }
    )
    Surface(
        modifier = Modifier.size(12.dp).semantics { contentDescription = desc },
        shape = CircleShape,
        color = color,
    ) {}
}

@Composable
private fun HostItem(host: Host, onClick: () -> Unit, dragModifier: Modifier = Modifier) {
    val monoFont = LocalFwMonoFont.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).then(dragModifier),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HostStatusDot(host)
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
                }
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
