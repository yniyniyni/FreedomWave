@file:OptIn(ExperimentalMaterial3Api::class)

package art.yniyniyni.freedomwave.ui.feature.squads

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import art.yniyniyni.freedomwave.ui.components.WaveLoader
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.ui.components.FwDetailTopBar
import art.yniyniyni.freedomwave.ui.l10n.resolve
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_cancel
import freedomwave.composeapp.generated.resources.common_delete
import freedomwave.composeapp.generated.resources.squads_delete_confirm
import freedomwave.composeapp.generated.resources.squads_delete_title
import freedomwave.composeapp.generated.resources.squads_edit_add_header
import freedomwave.composeapp.generated.resources.squads_edit_add_remark
import freedomwave.composeapp.generated.resources.squads_edit_basic
import freedomwave.composeapp.generated.resources.squads_edit_delete
import freedomwave.composeapp.generated.resources.squads_edit_enable_override
import freedomwave.composeapp.generated.resources.squads_edit_fallback_limit
import freedomwave.composeapp.generated.resources.squads_edit_happ_announce
import freedomwave.composeapp.generated.resources.squads_edit_happ_routing
import freedomwave.composeapp.generated.resources.squads_edit_header_name
import freedomwave.composeapp.generated.resources.squads_edit_header_value
import freedomwave.composeapp.generated.resources.squads_edit_headers
import freedomwave.composeapp.generated.resources.squads_edit_hosts
import freedomwave.composeapp.generated.resources.squads_edit_hwid
import freedomwave.composeapp.generated.resources.squads_edit_hwid_enabled
import freedomwave.composeapp.generated.resources.squads_edit_max_announce
import freedomwave.composeapp.generated.resources.squads_edit_max_announce_hint
import freedomwave.composeapp.generated.resources.squads_edit_none
import freedomwave.composeapp.generated.resources.squads_edit_profile_title
import freedomwave.composeapp.generated.resources.squads_edit_randomize_hosts
import freedomwave.composeapp.generated.resources.squads_edit_remark_disabled
import freedomwave.composeapp.generated.resources.squads_edit_remark_empty_hosts
import freedomwave.composeapp.generated.resources.squads_edit_remark_expired
import freedomwave.composeapp.generated.resources.squads_edit_remark_hint
import freedomwave.composeapp.generated.resources.squads_edit_remark_hwid_max
import freedomwave.composeapp.generated.resources.squads_edit_remark_hwid_unsupported
import freedomwave.composeapp.generated.resources.squads_edit_remark_limited
import freedomwave.composeapp.generated.resources.squads_edit_remarks
import freedomwave.composeapp.generated.resources.squads_edit_remarks_required
import freedomwave.composeapp.generated.resources.squads_edit_save
import freedomwave.composeapp.generated.resources.squads_edit_serve_json
import freedomwave.composeapp.generated.resources.squads_edit_server_desc
import freedomwave.composeapp.generated.resources.squads_edit_settings
import freedomwave.composeapp.generated.resources.squads_edit_show_remarks
import freedomwave.composeapp.generated.resources.squads_edit_subpage
import freedomwave.composeapp.generated.resources.squads_edit_support_link
import freedomwave.composeapp.generated.resources.squads_edit_templates
import freedomwave.composeapp.generated.resources.squads_edit_update_interval
import freedomwave.composeapp.generated.resources.squads_edit_vless_route
import freedomwave.composeapp.generated.resources.squads_edit_webpage_url
import freedomwave.composeapp.generated.resources.squads_name
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val REMARK_CATEGORIES: List<Pair<RemarkCategory, StringResource>> = listOf(
    RemarkCategory.EXPIRED to Res.string.squads_edit_remark_expired,
    RemarkCategory.LIMITED to Res.string.squads_edit_remark_limited,
    RemarkCategory.DISABLED to Res.string.squads_edit_remark_disabled,
    RemarkCategory.EMPTY_HOSTS to Res.string.squads_edit_remark_empty_hosts,
    RemarkCategory.HWID_MAX to Res.string.squads_edit_remark_hwid_max,
    RemarkCategory.HWID_UNSUPPORTED to Res.string.squads_edit_remark_hwid_unsupported,
)

private fun remarkListFor(state: ExternalSquadEditUiState, cat: RemarkCategory): List<String> = when (cat) {
    RemarkCategory.EXPIRED -> state.expiredUsers
    RemarkCategory.LIMITED -> state.limitedUsers
    RemarkCategory.DISABLED -> state.disabledUsers
    RemarkCategory.EMPTY_HOSTS -> state.emptyHosts
    RemarkCategory.HWID_MAX -> state.hwidMaxDevicesExceeded
    RemarkCategory.HWID_UNSUPPORTED -> state.hwidNotSupported
}

@Composable
internal fun ExternalSquadEditScreen(
    vm: ExternalSquadEditViewModel,
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
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(Res.string.common_cancel)) }
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { FwDetailTopBar(title = state.name, onBack = onBack) },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                WaveLoader()
            }
            return@Scaffold
        }

        val noneLabel = stringResource(Res.string.squads_edit_none)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 1. Basic
            item {
                SquadSectionCard(stringResource(Res.string.squads_edit_basic), Icons.Rounded.Tune, MaterialTheme.colorScheme.primary) {
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

            // 2. Templates
            item {
                SquadSectionCard(stringResource(Res.string.squads_edit_templates), Icons.Rounded.Description, MaterialTheme.colorScheme.tertiary) {
                    state.templatesByType.forEach { (type, list) ->
                        SquadDropdown(
                            label = type,
                            selectedValue = state.selectedTemplates[type],
                            options = listOf<Pair<String?, String>>(null to noneLabel) + list.map { it.uuid to it.name },
                            onSelect = { vm.selectTemplate(type, it) },
                        )
                    }
                }
            }

            // 3. Settings
            item {
                SquadSectionCard(stringResource(Res.string.squads_edit_settings), Icons.Rounded.Tune, MaterialTheme.colorScheme.secondary) {
                    OverrideTextRow(
                        label = stringResource(Res.string.squads_edit_profile_title),
                        enabled = state.profileTitle != null,
                        onToggle = vm::toggleProfileTitle,
                        value = state.profileTitle ?: "",
                        onValue = vm::onProfileTitle,
                    )
                    OverrideTextRow(
                        label = stringResource(Res.string.squads_edit_support_link),
                        enabled = state.supportLink != null,
                        onToggle = vm::toggleSupportLink,
                        value = state.supportLink ?: "",
                        onValue = vm::onSupportLink,
                    )
                    OverrideTextRow(
                        label = stringResource(Res.string.squads_edit_update_interval),
                        enabled = state.profileUpdateInterval != null,
                        onToggle = vm::toggleProfileUpdateInterval,
                        value = state.profileUpdateInterval ?: "",
                        onValue = vm::onProfileUpdateInterval,
                        keyboardType = KeyboardType.Number,
                    )
                    OverrideTextRow(
                        label = stringResource(Res.string.squads_edit_happ_announce),
                        enabled = state.happAnnounce != null,
                        onToggle = vm::toggleHappAnnounce,
                        value = state.happAnnounce ?: "",
                        onValue = vm::onHappAnnounce,
                    )
                    OverrideTextRow(
                        label = stringResource(Res.string.squads_edit_happ_routing),
                        enabled = state.happRouting != null,
                        onToggle = vm::toggleHappRouting,
                        value = state.happRouting ?: "",
                        onValue = vm::onHappRouting,
                    )
                    OverrideSwitchRow(
                        label = stringResource(Res.string.squads_edit_webpage_url),
                        enabled = state.isProfileWebpageUrlEnabled != null,
                        onToggle = vm::toggleIsProfileWebpageUrlEnabled,
                        value = state.isProfileWebpageUrlEnabled ?: false,
                        onValue = vm::setIsProfileWebpageUrlEnabled,
                    )
                    OverrideSwitchRow(
                        label = stringResource(Res.string.squads_edit_serve_json),
                        enabled = state.serveJsonAtBaseSubscription != null,
                        onToggle = vm::toggleServeJsonAtBaseSubscription,
                        value = state.serveJsonAtBaseSubscription ?: false,
                        onValue = vm::setServeJsonAtBaseSubscription,
                    )
                    OverrideSwitchRow(
                        label = stringResource(Res.string.squads_edit_show_remarks),
                        enabled = state.isShowCustomRemarks != null,
                        onToggle = vm::toggleIsShowCustomRemarks,
                        value = state.isShowCustomRemarks ?: false,
                        onValue = vm::setIsShowCustomRemarks,
                    )
                    OverrideSwitchRow(
                        label = stringResource(Res.string.squads_edit_randomize_hosts),
                        enabled = state.randomizeHosts != null,
                        onToggle = vm::toggleRandomizeHosts,
                        value = state.randomizeHosts ?: false,
                        onValue = vm::setRandomizeHosts,
                    )
                }
            }

            // 4. Hosts
            item {
                SquadSectionCard(stringResource(Res.string.squads_edit_hosts), Icons.Rounded.Dns, MaterialTheme.colorScheme.primary) {
                    OverrideTextRow(
                        label = stringResource(Res.string.squads_edit_server_desc),
                        enabled = state.serverDescription != null,
                        onToggle = vm::toggleServerDescription,
                        value = state.serverDescription ?: "",
                        onValue = vm::onServerDescription,
                    )
                    OverrideTextRow(
                        label = stringResource(Res.string.squads_edit_vless_route),
                        enabled = state.vlessRouteId != null,
                        onToggle = vm::toggleVlessRouteId,
                        value = state.vlessRouteId ?: "",
                        onValue = vm::onVlessRouteId,
                        keyboardType = KeyboardType.Number,
                    )
                }
            }

            // 5. Headers
            item {
                SquadSectionCard(stringResource(Res.string.squads_edit_headers), Icons.Rounded.Article, MaterialTheme.colorScheme.tertiary) {
                    state.headers.forEachIndexed { index, pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = pair.first,
                                onValueChange = { vm.updateHeaderName(index, it) },
                                shape = MaterialTheme.shapes.medium,
                                label = { Text(stringResource(Res.string.squads_edit_header_name)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = pair.second,
                                onValueChange = { vm.updateHeaderValue(index, it) },
                                shape = MaterialTheme.shapes.medium,
                                label = { Text(stringResource(Res.string.squads_edit_header_value)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { vm.removeHeader(index) }) {
                                Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    TextButton(onClick = vm::addHeader) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text(stringResource(Res.string.squads_edit_add_header))
                    }
                }
            }

            // 6. HWID
            item {
                SquadSectionCard(stringResource(Res.string.squads_edit_hwid), Icons.Rounded.Security, MaterialTheme.colorScheme.secondary) {
                    SwitchRow(stringResource(Res.string.squads_edit_enable_override), state.hwidOverride, vm::setHwidOverride)
                    if (state.hwidOverride) {
                        SwitchRow(stringResource(Res.string.squads_edit_hwid_enabled), state.hwidEnabled, vm::setHwidEnabled)
                        OutlinedTextField(
                            value = state.hwidFallbackDeviceLimit,
                            onValueChange = vm::onHwidFallback,
                            shape = MaterialTheme.shapes.medium,
                            label = { Text(stringResource(Res.string.squads_edit_fallback_limit)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = state.hwidMaxDevicesAnnounce,
                            onValueChange = vm::onHwidAnnounce,
                            shape = MaterialTheme.shapes.medium,
                            label = { Text(stringResource(Res.string.squads_edit_max_announce)) },
                            supportingText = { Text(stringResource(Res.string.squads_edit_max_announce_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // 7. Remarks
            item {
                SquadSectionCard(stringResource(Res.string.squads_edit_remarks), Icons.Rounded.Checklist, MaterialTheme.colorScheme.primary) {
                    SwitchRow(stringResource(Res.string.squads_edit_enable_override), state.remarksOverride, vm::setRemarksOverride)
                    if (state.remarksOverride) {
                        REMARK_CATEGORIES.forEach { (cat, labelRes) ->
                            Text(
                                stringResource(labelRes),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            remarkListFor(state, cat).forEachIndexed { index, value ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    OutlinedTextField(
                                        value = value,
                                        onValueChange = { vm.updateRemark(cat, index, it) },
                                        shape = MaterialTheme.shapes.medium,
                                        label = { Text(stringResource(Res.string.squads_edit_remark_hint)) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(onClick = { vm.removeRemark(cat, index) }) {
                                        Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            TextButton(onClick = { vm.addRemark(cat) }) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                Text(stringResource(Res.string.squads_edit_add_remark))
                            }
                        }
                        if (state.remarksError) {
                            Text(
                                stringResource(Res.string.squads_edit_remarks_required),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            // 8. Sub Page
            item {
                SquadSectionCard(stringResource(Res.string.squads_edit_subpage), Icons.Rounded.Palette, MaterialTheme.colorScheme.tertiary) {
                    SquadDropdown(
                        label = stringResource(Res.string.squads_edit_subpage),
                        selectedValue = state.subpageConfigUuid,
                        options = listOf<Pair<String?, String>>(null to noneLabel) + state.subPageConfigs.map { it.uuid to it.name },
                        onSelect = vm::selectSubpage,
                    )
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
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    }
                    Text(stringResource(Res.string.squads_edit_save))
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text(stringResource(Res.string.squads_edit_delete))
                }
            }
        }
    }
}
