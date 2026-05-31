package art.yniyniyni.freedomwave.ui.feature.hosts

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.domain.model.Host
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

    if (state.selected != null) {
        HostDetailScreen(
            host = state.selected!!,
            actionInProgress = state.actionInProgress,
            onBack = vm::clearSelection,
            onToggleEnabled = { vm.toggleEnabled(state.selected!!) },
            onDelete = { vm.delete(state.selected!!) }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Hosts (${state.hosts.size})") },
                    actions = { TextButton(onClick = vm::load) { Text("Refresh") } }
                )
            },
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when {
                    state.isLoading && state.hosts.isEmpty() ->
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                    state.error != null && state.hosts.isEmpty() ->
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) {
                                Text("Retry")
                            }
                        }

                    state.hosts.isEmpty() ->
                        Text(
                            "No hosts",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    else ->
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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

@Composable
private fun HostItem(host: Host, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(host.remark, fontWeight = FontWeight.Medium)
                Text(
                    if (host.isDisabled) "Disabled" else "Enabled",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (host.isDisabled) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "${host.address}:${host.port}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(host.securityLayer, style = MaterialTheme.typography.labelSmall)
                host.tag?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
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
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete host") },
            text = { Text("Delete \"${host.remark}\"? This cannot be undone.") },
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
                title = { Text(host.remark) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Back") }
                }
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
                        Text("Connection", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        InfoRow("Address", "${host.address}:${host.port}")
                        InfoRow("Security", host.securityLayer)
                        host.sni?.let { InfoRow("SNI", it) }
                        host.path?.let { InfoRow("Path", it) }
                        host.host?.let { InfoRow("Host header", it) }
                        host.alpn?.let { InfoRow("ALPN", it) }
                        host.fingerprint?.let { InfoRow("Fingerprint", it) }
                        if (host.allowInsecure) InfoRow("Insecure TLS", "Allowed")
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        InfoRow("Status", if (host.isDisabled) "Disabled" else "Enabled")
                        host.tag?.let { InfoRow("Tag", it) }
                        host.serverDescription?.let { InfoRow("Description", it) }
                        if (host.isHidden) InfoRow("Visibility", "Hidden")
                        if (host.shuffleHost) InfoRow("Shuffle host", "Yes")
                        if (host.nodes.isNotEmpty()) InfoRow("Nodes", "${host.nodes.size}")
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onToggleEnabled,
                        enabled = !actionInProgress,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (host.isDisabled) "Enable" else "Disable")
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = { showDeleteDialog = true },
                        enabled = !actionInProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete")
                    }
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
