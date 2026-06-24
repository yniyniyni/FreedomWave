@file:OptIn(ExperimentalMaterial3Api::class)

package art.yniyniyni.freedomwave.ui.feature.squads

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import art.yniyniyni.freedomwave.ui.components.WaveLoader
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.ui.components.FwDetailTopBar
import art.yniyniyni.freedomwave.ui.l10n.resolve
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_cancel
import freedomwave.composeapp.generated.resources.common_delete
import freedomwave.composeapp.generated.resources.nodes_form_deselect_all
import freedomwave.composeapp.generated.resources.nodes_form_select_all
import freedomwave.composeapp.generated.resources.squads_delete_confirm
import freedomwave.composeapp.generated.resources.squads_delete_title
import freedomwave.composeapp.generated.resources.squads_edit_basic
import freedomwave.composeapp.generated.resources.squads_edit_delete
import freedomwave.composeapp.generated.resources.squads_edit_inbounds
import freedomwave.composeapp.generated.resources.squads_edit_no_inbounds
import freedomwave.composeapp.generated.resources.squads_edit_save
import freedomwave.composeapp.generated.resources.squads_edit_select_inbounds
import freedomwave.composeapp.generated.resources.squads_name
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InternalSquadEditScreen(
    vm: InternalSquadEditViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDelete: () -> Unit,
) {
    val state by vm.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(Res.string.squads_delete_title)) },
            text = { Text(stringResource(Res.string.squads_delete_confirm, state.name)) },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDelete() },
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(Res.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(Res.string.common_cancel))
                }
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            FwDetailTopBar(title = state.name, onBack = onBack)
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                WaveLoader()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Basic section
            item {
                SquadSectionCard(
                    title = stringResource(Res.string.squads_edit_basic),
                    icon = Icons.Rounded.Tune,
                    tint = MaterialTheme.colorScheme.primary,
                ) {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = vm::onName,
                        shape = MaterialTheme.shapes.medium,
                        label = { Text(stringResource(Res.string.squads_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Inbounds section
            item {
                SquadSectionCard(
                    title = stringResource(Res.string.squads_edit_inbounds),
                    icon = Icons.Rounded.Dns,
                    tint = MaterialTheme.colorScheme.tertiary,
                ) {
                    if (state.profiles.isEmpty()) {
                        Text(
                            stringResource(Res.string.squads_edit_no_inbounds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            stringResource(Res.string.squads_edit_select_inbounds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        state.profiles.forEach { profile ->
                            // Profile header row with select/deselect all
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    profile.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f),
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        stringResource(Res.string.nodes_form_select_all),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { vm.selectAllInbounds(profile.uuid) },
                                    )
                                    Text(
                                        stringResource(Res.string.nodes_form_deselect_all),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { vm.deselectAllInbounds(profile.uuid) },
                                    )
                                }
                            }
                            // Inbound rows
                            profile.inbounds.forEach { ib ->
                                val checked = ib.uuid in state.selectedInbounds
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { vm.toggleInbound(ib.uuid) }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { vm.toggleInbound(ib.uuid) },
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            ib.tag,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        val meta = listOfNotNull(ib.type, ib.network, ib.security).joinToString(" · ")
                                        if (meta.isNotEmpty()) {
                                            Text(
                                                meta,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    ib.port?.let {
                                        Text(
                                            ":$it",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Members section
            item {
                SquadMembersCard(
                    expanded = state.membersExpanded,
                    loading = state.membersLoading,
                    members = state.members,
                    count = state.memberCount,
                    error = state.membersError?.resolve(),
                    onToggle = vm::toggleMembers,
                    onRetry = vm::loadMembers,
                )
            }

            // Error + actions
            item {
                state.actionError?.let {
                    Text(
                        it.resolve(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Button(
                    onClick = { vm.submit(onSaved) },
                    enabled = state.canSave,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(percent = 50),
                ) {
                    if (state.isSaving) {
                        WaveLoader(modifier = Modifier.padding(end = 8.dp).size(width = 28.dp, height = 18.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                    }
                    Text(stringResource(Res.string.squads_edit_save))
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(
                        Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Text(stringResource(Res.string.squads_edit_delete))
                }
            }
        }
    }
}
