package art.yniyniyni.freedomwave.ui.feature.users

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
import androidx.compose.material3.Badge
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.domain.model.User
import art.yniyniyni.freedomwave.domain.model.UserStatus
import art.yniyniyni.freedomwave.util.formatBytes
import org.koin.compose.viewmodel.koinViewModel

private sealed interface UsersNav {
    object List : UsersNav
    data class Detail(val user: User) : UsersNav
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(vm: UsersViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var nav: UsersNav by remember { mutableStateOf(UsersNav.List) }

    LaunchedEffect(state.actionError) {
        state.actionError?.let {
            snackbar.showSnackbar(it)
            vm.clearActionError()
        }
    }

    // Keep detail user in sync with list updates (after edit or action)
    LaunchedEffect(state.users) {
        val current = nav as? UsersNav.Detail ?: return@LaunchedEffect
        val updated = state.users.find { it.uuid == current.user.uuid } ?: return@LaunchedEffect
        if (updated != current.user) nav = UsersNav.Detail(updated)
    }

    // Form takes full screen priority
    if (state.formVisible) {
        UserCreateEditScreen(state, vm)
        return
    }

    when (val current = nav) {
        is UsersNav.List ->
            Scaffold(
                snackbarHost = { SnackbarHost(snackbar) },
                topBar = {
                    TopAppBar(title = { Text("Users (${state.filtered.size})") },
                        actions = { TextButton(onClick = vm::load) { Text("Refresh") } })
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = vm::openCreateForm) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            ) { padding ->
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = vm::onQueryChange,
                        placeholder = { Text("Search by username or tag") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    when {
                        state.isLoading && state.users.isEmpty() ->
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        state.error != null && state.users.isEmpty() ->
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                                    Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
                                }
                            }
                        else ->
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.filtered, key = { it.uuid }) { user ->
                                    UserListItem(
                                        user = user,
                                        onClick = { nav = UsersNav.Detail(user) }
                                    )
                                }
                            }
                    }
                }
            }

        is UsersNav.Detail ->
            UserDetailScreen(
                user = current.user,
                onBack = { nav = UsersNav.List },
                onEdit = { vm.openEditForm(current.user) },
                onEnable = { vm.enableUser(current.user.uuid) },
                onDisable = { vm.disableUser(current.user.uuid) },
                onResetTraffic = { vm.resetTraffic(current.user.uuid) },
                onDelete = { vm.deleteUser(current.user.uuid); nav = UsersNav.List }
            )
    }
}

@Composable
private fun UserListItem(user: User, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(user.username, fontWeight = FontWeight.Medium)
                    StatusBadge(user.status)
                }
                if (!user.tag.isNullOrBlank()) {
                    Text(user.tag, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    formatBytes(user.usedTrafficBytes) +
                    if (user.trafficLimitBytes > 0) " / ${formatBytes(user.trafficLimitBytes)}" else " / ∞",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: UserStatus) {
    val color = when (status) {
        UserStatus.ACTIVE   -> Color(0xFF4CAF50)
        UserStatus.DISABLED -> MaterialTheme.colorScheme.onSurfaceVariant
        UserStatus.LIMITED  -> Color(0xFFFF9800)
        UserStatus.EXPIRED  -> MaterialTheme.colorScheme.error
    }
    Badge(containerColor = color) {
        Text(status.label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDetailScreen(
    user: User,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onResetTraffic: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete user") },
            text = { Text("Delete ${user.username}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user.username) },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        InfoRow("Status", user.status.label)
                        InfoRow("UUID", user.shortUuid)
                        InfoRow("Strategy", user.trafficLimitStrategy)
                        InfoRow("Expires", user.expireAt.take(10))
                        user.email?.let { InfoRow("Email", it) }
                        user.tag?.let { InfoRow("Tag", it) }
                        user.description?.let { InfoRow("Notes", it) }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Traffic", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        InfoRow("Used", formatBytes(user.usedTrafficBytes))
                        InfoRow("Limit", if (user.trafficLimitBytes > 0) formatBytes(user.trafficLimitBytes) else "Unlimited")
                        InfoRow("Lifetime", formatBytes(user.lifetimeUsedTrafficBytes))
                    }
                }
            }
            if (user.activeSquads.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Squads", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            user.activeSquads.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                        Text("Edit User")
                    }
                    if (user.isActive) {
                        Button(
                            onClick = onDisable,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) { Text("Disable User") }
                    } else {
                        Button(onClick = onEnable, modifier = Modifier.fillMaxWidth()) { Text("Enable User") }
                    }
                    Button(
                        onClick = onResetTraffic,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) { Text("Reset Traffic") }
                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete User") }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
