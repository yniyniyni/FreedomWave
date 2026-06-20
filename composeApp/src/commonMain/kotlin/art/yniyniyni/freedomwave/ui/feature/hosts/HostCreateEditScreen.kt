@file:OptIn(ExperimentalMaterial3Api::class)

package art.yniyniyni.freedomwave.ui.feature.hosts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import art.yniyniyni.freedomwave.domain.model.ConfigProfile
import art.yniyniyni.freedomwave.domain.model.Node
import art.yniyniyni.freedomwave.ui.components.FwDetailTopBar
import art.yniyniyni.freedomwave.ui.components.FwSectionIcon
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import art.yniyniyni.freedomwave.ui.theme.LocalFwStatus
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_cancel
import freedomwave.composeapp.generated.resources.common_delete
import freedomwave.composeapp.generated.resources.hosts_delete_confirm
import freedomwave.composeapp.generated.resources.hosts_delete_title
import freedomwave.composeapp.generated.resources.hosts_form_enabled
import freedomwave.composeapp.generated.resources.hosts_status_disabled
import freedomwave.composeapp.generated.resources.hosts_status_enabled
import freedomwave.composeapp.generated.resources.hosts_form_address
import freedomwave.composeapp.generated.resources.hosts_form_allow_insecure
import freedomwave.composeapp.generated.resources.hosts_form_alpn
import freedomwave.composeapp.generated.resources.hosts_form_assigned_nodes
import freedomwave.composeapp.generated.resources.hosts_form_basic
import freedomwave.composeapp.generated.resources.hosts_form_create
import freedomwave.composeapp.generated.resources.hosts_form_done
import freedomwave.composeapp.generated.resources.hosts_form_edit_title
import freedomwave.composeapp.generated.resources.hosts_form_fingerprint
import freedomwave.composeapp.generated.resources.hosts_form_hidden
import freedomwave.composeapp.generated.resources.hosts_form_host
import freedomwave.composeapp.generated.resources.hosts_form_host_hint
import freedomwave.composeapp.generated.resources.hosts_form_inbound
import freedomwave.composeapp.generated.resources.hosts_form_keep_sni_blank
import freedomwave.composeapp.generated.resources.hosts_form_mihomo
import freedomwave.composeapp.generated.resources.hosts_form_misc
import freedomwave.composeapp.generated.resources.hosts_form_new_title
import freedomwave.composeapp.generated.resources.hosts_form_no_inbounds
import freedomwave.composeapp.generated.resources.hosts_form_no_nodes
import freedomwave.composeapp.generated.resources.hosts_form_none
import freedomwave.composeapp.generated.resources.hosts_form_override_sni
import freedomwave.composeapp.generated.resources.hosts_form_overrides
import freedomwave.composeapp.generated.resources.hosts_form_path
import freedomwave.composeapp.generated.resources.hosts_form_path_hint
import freedomwave.composeapp.generated.resources.hosts_form_port
import freedomwave.composeapp.generated.resources.hosts_form_remark
import freedomwave.composeapp.generated.resources.hosts_form_remark_hint
import freedomwave.composeapp.generated.resources.hosts_form_save
import freedomwave.composeapp.generated.resources.hosts_form_security
import freedomwave.composeapp.generated.resources.hosts_form_security_default
import freedomwave.composeapp.generated.resources.hosts_form_security_none
import freedomwave.composeapp.generated.resources.hosts_form_security_tls
import freedomwave.composeapp.generated.resources.hosts_form_select_inbound
import freedomwave.composeapp.generated.resources.hosts_form_select_nodes
import freedomwave.composeapp.generated.resources.hosts_form_server_desc
import freedomwave.composeapp.generated.resources.hosts_form_server_desc_hint
import freedomwave.composeapp.generated.resources.hosts_form_shuffle
import freedomwave.composeapp.generated.resources.hosts_form_sni
import freedomwave.composeapp.generated.resources.hosts_form_sni_hint
import freedomwave.composeapp.generated.resources.hosts_form_tag
import freedomwave.composeapp.generated.resources.hosts_form_tag_hint
import freedomwave.composeapp.generated.resources.hosts_form_vless_route_hint
import freedomwave.composeapp.generated.resources.hosts_form_vless_route_id
import freedomwave.composeapp.generated.resources.hosts_form_xray
import freedomwave.composeapp.generated.resources.hosts_form_xray_template
import org.jetbrains.compose.resources.stringResource

private val ALPN_OPTIONS = listOf("h3", "h2", "http/1.1", "h2,http/1.1", "h3,h2,http/1.1", "h3,h2")
private val FINGERPRINT_OPTIONS = listOf("chrome", "firefox", "safari", "ios", "android", "edge", "qq", "random", "randomized")

@Composable
internal fun HostCreateEditScreen(
    vm: HostFormViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val state by vm.state.collectAsState()
    val isEdit = state.isEdit

    var showInboundPicker by remember { mutableStateOf(false) }
    var showNodePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(Res.string.hosts_delete_title)) },
            text = { Text(stringResource(Res.string.hosts_delete_confirm, state.remark)) },
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

    if (showInboundPicker) {
        InboundPickerDialog(
            profiles = state.profiles,
            selectedInboundUuid = state.selectedInboundUuid,
            onPick = { profileUuid, inboundUuid ->
                vm.selectInbound(profileUuid, inboundUuid)
                showInboundPicker = false
            },
            onDismiss = { showInboundPicker = false },
        )
    }

    if (showNodePicker) {
        NodePickerDialog(
            nodes = state.nodes,
            selected = state.selectedNodeUuids,
            onToggle = vm::toggleNode,
            onDone = { showNodePicker = false },
            onDismiss = { showNodePicker = false },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            FwDetailTopBar(
                title = if (isEdit) stringResource(Res.string.hosts_form_edit_title, state.remark)
                else stringResource(Res.string.hosts_form_new_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        val noneLabel = stringResource(Res.string.hosts_form_none)
        val alpnOptions: List<Pair<String?, String>> =
            listOf<Pair<String?, String>>(null to noneLabel) + ALPN_OPTIONS.map { it to it }
        val fingerprintOptions: List<Pair<String?, String>> =
            listOf<Pair<String?, String>>(null to noneLabel) + FINGERPRINT_OPTIONS.map { it to it }
        val xrayTemplateOptions: List<Pair<String?, String>> =
            listOf<Pair<String?, String>>(null to noneLabel) + state.xrayTemplates.map { it.uuid to it.name }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 0. Header (edit mode only) — live summary
            if (isEdit) {
                item {
                    HostHeaderCard(
                        remark = state.remark,
                        address = state.address,
                        port = state.port,
                        isDisabled = state.isDisabled,
                    )
                }
            }

            // 1. Basic
            item {
                FormCard(stringResource(Res.string.hosts_form_basic), Icons.Rounded.Tune, MaterialTheme.colorScheme.primary) {
                    SwitchRow(stringResource(Res.string.hosts_form_enabled), !state.isDisabled) { vm.setDisabled(!it) }
                    OutlinedTextField(
                        value = state.remark,
                        onValueChange = vm::onRemark,
                        label = { Text(stringResource(Res.string.hosts_form_remark)) },
                        placeholder = { Text(stringResource(Res.string.hosts_form_remark_hint)) },
                        singleLine = true,
                        isError = state.remarkError != null,
                        supportingText = state.remarkError?.let { { Text(it.resolve()) } },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // Inbound selector — read-only, clickable
                    OutlinedTextField(
                        value = state.selectedInboundTag ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text(stringResource(Res.string.hosts_form_inbound)) },
                        placeholder = { Text(stringResource(Res.string.hosts_form_select_inbound)) },
                        modifier = Modifier.fillMaxWidth().clickable { showInboundPicker = true },
                    )
                    // Address + Port row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.address,
                            onValueChange = vm::onAddress,
                            label = { Text(stringResource(Res.string.hosts_form_address)) },
                            placeholder = { Text("example.com") },
                            singleLine = true,
                            modifier = Modifier.weight(2f),
                        )
                        OutlinedTextField(
                            value = state.port,
                            onValueChange = vm::onPort,
                            label = { Text(stringResource(Res.string.hosts_form_port)) },
                            placeholder = { Text("443") },
                            singleLine = true,
                            isError = state.portError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    OutlinedTextField(
                        value = state.tag,
                        onValueChange = vm::onTag,
                        label = { Text(stringResource(Res.string.hosts_form_tag)) },
                        placeholder = { Text(stringResource(Res.string.hosts_form_tag_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SwitchRow(stringResource(Res.string.hosts_form_hidden), state.isHidden, vm::setHidden)
                }
            }

            // 2. Connection Overrides
            item {
                FormCard(stringResource(Res.string.hosts_form_overrides), Icons.Rounded.Link, MaterialTheme.colorScheme.tertiary) {
                    OutlinedTextField(
                        value = state.sni,
                        onValueChange = vm::onSni,
                        label = { Text(stringResource(Res.string.hosts_form_sni)) },
                        supportingText = { Text(stringResource(Res.string.hosts_form_sni_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.host,
                        onValueChange = vm::onHost,
                        label = { Text(stringResource(Res.string.hosts_form_host)) },
                        placeholder = { Text(stringResource(Res.string.hosts_form_host_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.path,
                        onValueChange = vm::onPath,
                        label = { Text(stringResource(Res.string.hosts_form_path)) },
                        placeholder = { Text(stringResource(Res.string.hosts_form_path_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // Security segmented control
                    Text(
                        stringResource(Res.string.hosts_form_security),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.securityLayer == "DEFAULT",
                            onClick = { vm.setSecurityLayer("DEFAULT") },
                            label = { Text(stringResource(Res.string.hosts_form_security_default)) },
                        )
                        FilterChip(
                            selected = state.securityLayer == "TLS",
                            onClick = { vm.setSecurityLayer("TLS") },
                            label = { Text(stringResource(Res.string.hosts_form_security_tls)) },
                        )
                        FilterChip(
                            selected = state.securityLayer == "NONE",
                            onClick = { vm.setSecurityLayer("NONE") },
                            label = { Text(stringResource(Res.string.hosts_form_security_none)) },
                        )
                    }
                    DropdownPicker(
                        label = stringResource(Res.string.hosts_form_alpn),
                        selectedValue = state.alpn,
                        options = alpnOptions,
                        onSelect = vm::setAlpn,
                    )
                    DropdownPicker(
                        label = stringResource(Res.string.hosts_form_fingerprint),
                        selectedValue = state.fingerprint,
                        options = fingerprintOptions,
                        onSelect = vm::setFingerprint,
                    )
                    OutlinedTextField(
                        value = state.vlessRouteId,
                        onValueChange = vm::onVlessRouteId,
                        label = { Text(stringResource(Res.string.hosts_form_vless_route_id)) },
                        supportingText = { Text(stringResource(Res.string.hosts_form_vless_route_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SwitchRow(stringResource(Res.string.hosts_form_override_sni), state.overrideSniFromAddress, vm::setOverrideSni)
                    SwitchRow(stringResource(Res.string.hosts_form_keep_sni_blank), state.keepSniBlank, vm::setKeepSniBlank)
                    SwitchRow(stringResource(Res.string.hosts_form_allow_insecure), state.allowInsecure, vm::setAllowInsecure)
                }
            }

            // 3. Misc Settings
            item {
                FormCard(stringResource(Res.string.hosts_form_misc), Icons.Rounded.Settings, MaterialTheme.colorScheme.secondary) {
                    OutlinedTextField(
                        value = state.serverDescription,
                        onValueChange = vm::onServerDescription,
                        label = { Text(stringResource(Res.string.hosts_form_server_desc)) },
                        supportingText = { Text(stringResource(Res.string.hosts_form_server_desc_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SwitchRow(stringResource(Res.string.hosts_form_shuffle), state.shuffleHost, vm::setShuffleHost)
                    SwitchRow(stringResource(Res.string.hosts_form_mihomo), state.mihomoX25519, vm::setMihomo)
                }
            }

            // 4. Xray JSON & Raw
            item {
                FormCard(stringResource(Res.string.hosts_form_xray), Icons.Rounded.Description, MaterialTheme.colorScheme.primary) {
                    DropdownPicker(
                        label = stringResource(Res.string.hosts_form_xray_template),
                        selectedValue = state.xrayTemplateUuid,
                        options = xrayTemplateOptions,
                        onSelect = vm::selectXrayTemplate,
                    )
                }
            }

            // 5. Assigned Nodes
            item {
                FormCard(stringResource(Res.string.hosts_form_assigned_nodes), Icons.Rounded.Dns, MaterialTheme.colorScheme.tertiary) {
                    if (state.nodes.isEmpty()) {
                        Text(
                            stringResource(Res.string.hosts_form_no_nodes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        val nodeCount = state.selectedNodeUuids.size
                        OutlinedTextField(
                            value = if (nodeCount > 0) nodeCount.toString() else "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text(stringResource(Res.string.hosts_form_assigned_nodes)) },
                            placeholder = { Text(stringResource(Res.string.hosts_form_select_nodes)) },
                            modifier = Modifier.fillMaxWidth().clickable { showNodePicker = true },
                        )
                    }
                }
            }

            // Error + Save button
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
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp).heightIn(max = 18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            if (isEdit) Icons.Rounded.Check else Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                    }
                    Text(stringResource(if (isEdit) Res.string.hosts_form_save else Res.string.hosts_form_create))
                }

                if (onDelete != null) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text(stringResource(Res.string.common_delete))
                    }
                }
            }
        }
    }
}

@Composable
private fun HostHeaderCard(remark: String, address: String, port: String, isDisabled: Boolean) {
    val fwStatus = LocalFwStatus.current
    val monoFont = LocalFwMonoFont.current
    val statusColor = if (isDisabled) fwStatus.neutral else fwStatus.online
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(statusColor))
                    Text(
                        remark.ifBlank { "—" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                }
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        stringResource(if (isDisabled) Res.string.hosts_status_disabled else Res.string.hosts_status_enabled).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                    )
                }
            }
            if (address.isNotBlank()) {
                Text(
                    "$address:${port.ifBlank { "—" }}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FormCard(title: String, icon: ImageVector, tint: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FwSectionIcon(icon, tint)
                Text(title, style = MaterialTheme.typography.titleSmall)
            }
            content()
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DropdownPicker(
    label: String,
    selectedValue: String?,
    options: List<Pair<String?, String>>,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = options.find { it.first == selectedValue }?.second ?: options.firstOrNull()?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.second) },
                    onClick = { onSelect(option.first); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun InboundPickerDialog(
    profiles: List<ConfigProfile>,
    selectedInboundUuid: String?,
    onPick: (profileUuid: String, inboundUuid: String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                Modifier.padding(16.dp).heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (profiles.isEmpty()) {
                    Text(
                        stringResource(Res.string.hosts_form_no_inbounds),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        profiles.forEach { profile ->
                            item(key = "header_${profile.uuid}") {
                                Text(
                                    profile.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            }
                            items(profile.inbounds, key = { it.uuid }) { ib ->
                                val isSelected = ib.uuid == selectedInboundUuid
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPick(profile.uuid, ib.uuid) }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        if (isSelected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            ib.tag,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            listOfNotNull(ib.type, ib.network, ib.security).joinToString(" · "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
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
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(Res.string.common_cancel))
                }
            }
        }
    }
}

@Composable
private fun NodePickerDialog(
    nodes: List<Node>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                Modifier.padding(16.dp).heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (nodes.isEmpty()) {
                    Text(
                        stringResource(Res.string.hosts_form_no_nodes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(nodes, key = { it.uuid }) { n ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggle(n.uuid) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Checkbox(
                                    checked = n.uuid in selected,
                                    onCheckedChange = { onToggle(n.uuid) },
                                )
                                Text(n.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.common_cancel))
                    }
                    TextButton(onClick = onDone) {
                        Text(stringResource(Res.string.hosts_form_done))
                    }
                }
            }
        }
    }
}
