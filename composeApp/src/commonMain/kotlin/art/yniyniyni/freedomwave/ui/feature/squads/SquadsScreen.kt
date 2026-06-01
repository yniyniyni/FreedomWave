package art.yniyniyni.freedomwave.ui.feature.squads

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.domain.model.Squad
import art.yniyniyni.freedomwave.ui.components.ShimmerList
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadsScreen(vm: SquadsViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.actionError) {
        state.actionError?.let { vm.clearActionError(); snackbar.showSnackbar(it) }
    }

    // Dialogs
    if (state.showCreateDialog || state.showEditDialog) {
        val isEdit = state.showEditDialog
        AlertDialog(
            onDismissRequest = { if (!state.dialogIsLoading) vm.dismissDialog() },
            title = { Text(if (isEdit) "Rename squad" else "New squad") },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.dialogName,
                        onValueChange = vm::onDialogNameChange,
                        label = { Text("Name") },
                        singleLine = true,
                        enabled = !state.dialogIsLoading,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (isEdit) vm.updateSquad() else vm.createSquad()
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isEdit && state.activeTab == 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Inbound configuration can be set in the web panel after creation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    state.dialogError?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { if (isEdit) vm.updateSquad() else vm.createSquad() },
                    enabled = !state.dialogIsLoading
                ) {
                    if (state.dialogIsLoading) CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                    else Text(if (isEdit) "Save" else "Create")
                }
            },
            dismissButton = {
                TextButton(onClick = vm::dismissDialog, enabled = !state.dialogIsLoading) { Text("Cancel") }
            }
        )
    }

    if (state.selected != null) {
        SquadDetailScreen(
            squad = state.selected!!,
            actionInProgress = state.actionInProgress,
            onBack = vm::clearSelection,
            onEdit = { vm.openEditDialog(state.selected!!) },
            onDelete = { vm.deleteSquad(state.selected!!) }
        )
        return
    }

    val currentList = if (state.activeTab == 0) state.internalSquads else state.externalSquads

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Squads") },
                actions = { TextButton(onClick = vm::load) { Text("Refresh") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = vm::openCreateDialog) { Text("+") }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = state.activeTab) {
                Tab(selected = state.activeTab == 0, onClick = { vm.setTab(0) },
                    text = { Text("Internal (${state.internalSquads.size})") })
                Tab(selected = state.activeTab == 1, onClick = { vm.setTab(1) },
                    text = { Text("External (${state.externalSquads.size})") })
            }

            when {
                state.isLoading && currentList.isEmpty() ->
                    ShimmerList()

                state.error != null && currentList.isEmpty() ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
                        }
                    }

                else ->
                    PullToRefreshBox(
                        isRefreshing = state.isLoading && currentList.isNotEmpty(),
                        onRefresh = vm::load,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (currentList.isEmpty()) {
                            Text(
                                "No squads. Tap + to create one.",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(currentList, key = { it.uuid }) { squad ->
                                    SquadItem(squad = squad, onClick = { vm.select(squad) })
                                }
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun SquadItem(squad: Squad, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(squad.name, fontWeight = FontWeight.Medium)
                Text(
                    "${squad.membersCount} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            squad.inboundsCount?.let {
                Text(
                    "$it inbounds",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SquadDetailScreen(
    squad: Squad,
    actionInProgress: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete squad") },
            text = { Text("Delete \"${squad.name}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(squad.name) },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        InfoRow("Name", squad.name)
                        InfoRow("Type", if (squad.type == Squad.Type.INTERNAL) "Internal" else "External")
                        InfoRow("Members", squad.membersCount.toString())
                        squad.inboundsCount?.let { InfoRow("Inbounds", it.toString()) }
                        InfoRow("Created", squad.createdAt.take(10))
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onEdit,
                        enabled = !actionInProgress,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Rename") }

                    Button(
                        onClick = { showDeleteDialog = true },
                        enabled = !actionInProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Delete") }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
