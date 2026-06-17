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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import art.yniyniyni.freedomwave.ui.navigation.BackGestureEffect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_refresh
import freedomwave.composeapp.generated.resources.common_retry
import freedomwave.composeapp.generated.resources.hosts_empty
import freedomwave.composeapp.generated.resources.hosts_form_add
import freedomwave.composeapp.generated.resources.hosts_status_disabled
import freedomwave.composeapp.generated.resources.hosts_title_count
import art.yniyniyni.freedomwave.ui.components.ShimmerList
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private sealed interface HostsNav {
    data object List : HostsNav
    data class Editor(val host: Host?, val epoch: Int) : HostsNav   // null = create

    val depth: Int get() = if (this is Editor) 1 else 0
    val key: String get() = when (this) {
        List -> "list"
        is Editor -> "editor:${host?.uuid ?: "new"}:$epoch"
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
                onOpenHost = { host -> formEpoch++; stack = stack + HostsNav.Editor(host, formEpoch) },
                onCreate   = { formEpoch++; stack = stack + HostsNav.Editor(null, formEpoch) },
            )
            is HostsNav.Editor -> {
                val uuid = navEntry.host?.uuid
                val formVm: HostFormViewModel = koinViewModel(key = "host-form-${navEntry.epoch}") { parametersOf(uuid) }
                HostCreateEditScreen(
                    vm      = formVm,
                    onBack  = { stack = stack.dropLast(1) },
                    onSaved = {
                        vm.load()
                        if (stack.lastOrNull() is HostsNav.Editor) stack = stack.dropLast(1)
                    },
                    onDelete = navEntry.host?.let { host ->
                        {
                            vm.delete(host)
                            if (stack.lastOrNull() is HostsNav.Editor) stack = stack.dropLast(1)
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
                                HostItem(host = host, onClick = { onOpenHost(host) })
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
