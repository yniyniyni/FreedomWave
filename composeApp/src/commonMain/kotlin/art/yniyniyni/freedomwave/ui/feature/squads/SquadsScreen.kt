package art.yniyniyni.freedomwave.ui.feature.squads

import androidx.compose.animation.animateColorAsState
import art.yniyniyni.freedomwave.ui.components.FwNavDestination
import art.yniyniyni.freedomwave.ui.components.FwNavigationContainer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import art.yniyniyni.freedomwave.ui.components.FwDetailTopBar
import art.yniyniyni.freedomwave.ui.components.FwTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.domain.model.Squad
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_cancel
import freedomwave.composeapp.generated.resources.common_create
import freedomwave.composeapp.generated.resources.common_refresh
import freedomwave.composeapp.generated.resources.common_retry
import freedomwave.composeapp.generated.resources.squads_empty
import freedomwave.composeapp.generated.resources.squads_inbound_hint
import freedomwave.composeapp.generated.resources.squads_inbounds
import freedomwave.composeapp.generated.resources.squads_members
import freedomwave.composeapp.generated.resources.squads_name
import freedomwave.composeapp.generated.resources.squads_new_squad
import freedomwave.composeapp.generated.resources.squads_title
import freedomwave.composeapp.generated.resources.squads_type_external
import freedomwave.composeapp.generated.resources.squads_type_internal
import art.yniyniyni.freedomwave.ui.components.ShimmerList
import art.yniyniyni.freedomwave.ui.l10n.resolve
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private sealed interface SquadsNav : FwNavDestination {
    data object List : SquadsNav
    data class InternalEditor(val squad: Squad, val epoch: Int) : SquadsNav
    data class ExternalEditor(val squad: Squad, val epoch: Int) : SquadsNav

    override val depth: Int get() = if (this is List) 0 else 1
    override val key: String get() = when (this) {
        List -> "list"
        is InternalEditor -> "int:${squad.uuid}:$epoch"
        is ExternalEditor -> "ext:${squad.uuid}:$epoch"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadsScreen(vm: SquadsViewModel = koinViewModel(), onBack: (() -> Unit)? = null) {
    val state by vm.state.collectAsState()

    var formEpoch by remember { mutableStateOf(0) }

    FwNavigationContainer<SquadsNav>(
        navLabel = "squads_nav",
        initialState = SquadsNav.List,
        initialStack = listOf(SquadsNav.List),
        actionError = state.actionError,
        onClearActionError = vm::clearActionError,
        contentKey = { it.key },
    ) { navEntry, push, pop, currentStack, snackbarHost ->
        when (navEntry) {
            is SquadsNav.List -> SquadsListContent(
                vm = vm,
                state = state,
                snackbar = snackbarHost,
                onBack = onBack,
                onOpenSquad = { squad ->
                    formEpoch++
                    push(
                        if (squad.type == Squad.Type.INTERNAL)
                            SquadsNav.InternalEditor(squad, formEpoch)
                        else
                            SquadsNav.ExternalEditor(squad, formEpoch)
                    )
                },
            )
            is SquadsNav.InternalEditor -> {
                val editVm: InternalSquadEditViewModel =
                    koinViewModel(key = "int-squad-${navEntry.epoch}") { parametersOf(navEntry.squad.uuid) }
                InternalSquadEditScreen(
                    vm = editVm,
                    onBack = { pop() },
                    onSaved = { vm.load(); if (currentStack().lastOrNull() is SquadsNav.InternalEditor) pop() },
                    onDelete = { vm.deleteSquad(navEntry.squad); if (currentStack().lastOrNull() is SquadsNav.InternalEditor) pop() },
                )
            }
            is SquadsNav.ExternalEditor -> {
                val editVm: ExternalSquadEditViewModel =
                    koinViewModel(key = "ext-squad-${navEntry.epoch}") { parametersOf(navEntry.squad.uuid) }
                ExternalSquadEditScreen(
                    vm = editVm,
                    onBack = { pop() },
                    onSaved = { vm.load(); if (currentStack().lastOrNull() is SquadsNav.ExternalEditor) pop() },
                    onDelete = { vm.deleteSquad(navEntry.squad); if (currentStack().lastOrNull() is SquadsNav.ExternalEditor) pop() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SquadsListContent(
    vm: SquadsViewModel,
    state: SquadsUiState,
    snackbar: SnackbarHostState,
    onBack: (() -> Unit)?,
    onOpenSquad: (Squad) -> Unit,
) {
    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { if (!state.dialogIsLoading) vm.dismissDialog() },
            title = { Text(stringResource(Res.string.squads_new_squad)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.dialogName,
                        onValueChange = vm::onDialogNameChange,
                        shape = MaterialTheme.shapes.medium,
                        label = { Text(stringResource(Res.string.squads_name)) },
                        singleLine = true,
                        enabled = !state.dialogIsLoading,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { vm.createSquad() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (state.activeTab == 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(Res.string.squads_inbound_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    state.dialogError?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it.resolve(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = vm::createSquad, enabled = !state.dialogIsLoading) {
                    if (state.dialogIsLoading) CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                    else Text(stringResource(Res.string.common_create))
                }
            },
            dismissButton = {
                TextButton(onClick = vm::dismissDialog, enabled = !state.dialogIsLoading) { Text(stringResource(Res.string.common_cancel)) }
            },
        )
    }

    val currentList = if (state.activeTab == 0) state.internalSquads else state.externalSquads

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            val refresh: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
                IconButton(onClick = vm::load) { Icon(Icons.Rounded.Refresh, contentDescription = stringResource(Res.string.common_refresh)) }
            }
            if (onBack != null) {
                FwDetailTopBar(title = stringResource(Res.string.squads_title), onBack = onBack, actions = refresh)
            } else {
                FwTopBar(title = stringResource(Res.string.squads_title), actions = refresh)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = vm::openCreateDialog,
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Rounded.Add, contentDescription = stringResource(Res.string.squads_new_squad)) }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SquadTypeSelector(
                activeTab = state.activeTab,
                internalCount = state.internalSquads.size,
                externalCount = state.externalSquads.size,
                onSelect = vm::setTab,
            )

            when {
                state.isLoading && currentList.isEmpty() -> ShimmerList()

                state.error != null && currentList.isEmpty() ->
                    Box(modifier = Modifier.fillMaxSize()) {
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
                        isRefreshing = state.isLoading && currentList.isNotEmpty(),
                        onRefresh = vm::load,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (currentList.isEmpty()) {
                            Text(
                                stringResource(Res.string.squads_empty),
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(currentList, key = { it.uuid }) { squad ->
                                    SquadItem(squad = squad, onClick = { onOpenSquad(squad) })
                                }
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun SquadTypeSelector(
    activeTab: Int,
    internalCount: Int,
    externalCount: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SquadSegment(
            modifier = Modifier.weight(1f),
            selected = activeTab == 0,
            accent = MaterialTheme.colorScheme.primary,
            onAccent = MaterialTheme.colorScheme.onPrimary,
            icon = Icons.Rounded.Groups,
            label = stringResource(Res.string.squads_type_internal),
            count = internalCount,
            onClick = { onSelect(0) },
        )
        SquadSegment(
            modifier = Modifier.weight(1f),
            selected = activeTab == 1,
            accent = MaterialTheme.colorScheme.tertiary,
            onAccent = MaterialTheme.colorScheme.onTertiary,
            icon = Icons.Rounded.CellTower,
            label = stringResource(Res.string.squads_type_external),
            count = externalCount,
            onClick = { onSelect(1) },
        )
    }
}

@Composable
private fun SquadSegment(
    modifier: Modifier,
    selected: Boolean,
    accent: Color,
    onAccent: Color,
    icon: ImageVector,
    label: String,
    count: Int,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(if (selected) accent else Color.Transparent, label = "squad_seg_bg")
    val content = if (selected) onAccent else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            maxLines = 1,
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(if (selected) onAccent.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 6.dp, vertical = 1.dp),
        ) {
            Text("$count", style = MaterialTheme.typography.labelSmall, color = content)
        }
    }
}

@Composable
private fun SquadItem(squad: Squad, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(squad.name, fontWeight = FontWeight.Medium)
                Text(
                    pluralStringResource(Res.plurals.squads_members, squad.membersCount, squad.membersCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            squad.inboundsCount?.let {
                Text(
                    pluralStringResource(Res.plurals.squads_inbounds, it, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
