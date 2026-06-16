@file:OptIn(ExperimentalMaterial3Api::class)

package art.yniyniyni.freedomwave.ui.feature.nodes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import art.yniyniyni.freedomwave.ui.components.FwDetailTopBar
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import art.yniyniyni.freedomwave.util.countryFlag
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_cancel
import freedomwave.composeapp.generated.resources.nodes_form_address
import freedomwave.composeapp.generated.resources.nodes_form_config_profile
import freedomwave.composeapp.generated.resources.nodes_form_connection
import freedomwave.composeapp.generated.resources.nodes_form_copy_compose
import freedomwave.composeapp.generated.resources.nodes_form_copy_secret
import freedomwave.composeapp.generated.resources.nodes_form_country
import freedomwave.composeapp.generated.resources.nodes_form_country_search
import freedomwave.composeapp.generated.resources.nodes_form_create
import freedomwave.composeapp.generated.resources.nodes_form_deselect_all
import freedomwave.composeapp.generated.resources.nodes_form_edit_title
import freedomwave.composeapp.generated.resources.nodes_form_inbounds
import freedomwave.composeapp.generated.resources.nodes_form_internal_name
import freedomwave.composeapp.generated.resources.nodes_form_internal_name_hint
import freedomwave.composeapp.generated.resources.nodes_form_multiplier
import freedomwave.composeapp.generated.resources.nodes_form_new_title
import freedomwave.composeapp.generated.resources.nodes_form_no_profiles
import freedomwave.composeapp.generated.resources.nodes_form_notify_at
import freedomwave.composeapp.generated.resources.nodes_form_port
import freedomwave.composeapp.generated.resources.nodes_form_reset_day
import freedomwave.composeapp.generated.resources.nodes_form_save
import freedomwave.composeapp.generated.resources.nodes_form_secret_key
import freedomwave.composeapp.generated.resources.nodes_form_secret_key_hint
import freedomwave.composeapp.generated.resources.nodes_form_select_all
import freedomwave.composeapp.generated.resources.nodes_form_select_profile
import freedomwave.composeapp.generated.resources.nodes_form_tags
import freedomwave.composeapp.generated.resources.nodes_form_tags_hint
import freedomwave.composeapp.generated.resources.nodes_form_traffic_limit_gb
import freedomwave.composeapp.generated.resources.nodes_form_traffic_tracking
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NodeCreateEditScreen(
    nodeUuid: String?,
    vm: NodeFormViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val monoFont = LocalFwMonoFont.current
    val clipboard = LocalClipboardManager.current
    val isEdit = state.isEdit
    var showCountryPicker by remember { mutableStateOf(false) }

    if (showCountryPicker) {
        CountryPickerDialog(
            onPick = { vm.onCountry(it); showCountryPicker = false },
            onDismiss = { showCountryPicker = false },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            FwDetailTopBar(
                title = if (isEdit) stringResource(Res.string.nodes_form_edit_title, state.name)
                else stringResource(Res.string.nodes_form_new_title),
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FormCard(stringResource(Res.string.nodes_form_connection), Icons.Rounded.Wifi) {
                    OutlinedTextField(
                        value = state.name, onValueChange = vm::onName,
                        label = { Text(stringResource(Res.string.nodes_form_internal_name)) },
                        placeholder = { Text(stringResource(Res.string.nodes_form_internal_name_hint)) },
                        singleLine = true, isError = state.nameError != null,
                        supportingText = state.nameError?.let { { Text(it.resolve()) } },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.address, onValueChange = vm::onAddress,
                            label = { Text(stringResource(Res.string.nodes_form_address)) },
                            placeholder = { Text("192.168.1.1") },
                            singleLine = true, isError = state.addressError != null,
                            supportingText = state.addressError?.let { { Text(it.resolve()) } },
                            modifier = Modifier.weight(2f),
                        )
                        OutlinedTextField(
                            value = state.port, onValueChange = vm::onPort,
                            label = { Text(stringResource(Res.string.nodes_form_port)) },
                            placeholder = { Text("2222") },
                            singleLine = true, isError = state.portError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    val country = countryFor(state.countryCode)
                    OutlinedTextField(
                        value = "${countryFlag(country.code)} ${country.name} (${country.code})",
                        onValueChange = {}, readOnly = true, enabled = false,
                        label = { Text(stringResource(Res.string.nodes_form_country)) },
                        modifier = Modifier.fillMaxWidth().clickable { showCountryPicker = true },
                    )
                }
            }

            item {
                FormCard(stringResource(Res.string.nodes_form_config_profile), Icons.Rounded.Settings) {
                    if (state.profiles.isEmpty()) {
                        Text(stringResource(Res.string.nodes_form_no_profiles),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(stringResource(Res.string.nodes_form_select_profile),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        state.profiles.forEach { profile ->
                            val selected = profile.uuid == state.selectedProfileUuid
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { vm.selectProfile(profile.uuid) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    if (selected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(profile.name, modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium)
                                Text(profile.inbounds.size.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        val selectedProfile = state.selectedProfile
                        if (selectedProfile != null && selectedProfile.inbounds.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(stringResource(Res.string.nodes_form_inbounds),
                                    style = MaterialTheme.typography.titleSmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(stringResource(Res.string.nodes_form_select_all),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { vm.selectAllInbounds() })
                                    Text(stringResource(Res.string.nodes_form_deselect_all),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable { vm.deselectAllInbounds() })
                                }
                            }
                            selectedProfile.inbounds.forEach { ib ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { vm.toggleInbound(ib.uuid) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Checkbox(checked = ib.uuid in state.selectedInbounds,
                                        onCheckedChange = { vm.toggleInbound(ib.uuid) })
                                    Column(Modifier.weight(1f)) {
                                        Text(ib.tag, style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium)
                                        Text(
                                            listOfNotNull(ib.type, ib.network, ib.security).joinToString(" · "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    ib.port?.let { Text(":$it", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                        }
                    }
                }
            }

            state.secretKey?.let { key ->
                item {
                    FormCard(stringResource(Res.string.nodes_form_secret_key), Icons.Rounded.Key) {
                        Text(stringResource(Res.string.nodes_form_secret_key_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(key, style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont),
                            maxLines = 3, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                        TextButton(onClick = { clipboard.setText(AnnotatedString(key)) },
                            modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp))
                            Text(stringResource(Res.string.nodes_form_copy_secret))
                        }
                        TextButton(
                            onClick = { clipboard.setText(AnnotatedString(buildNodeCompose(key, portOrNull(state.port)))) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp))
                            Text(stringResource(Res.string.nodes_form_copy_compose))
                        }
                    }
                }
            }

            item {
                FormCard(stringResource(Res.string.nodes_form_traffic_tracking), Icons.Rounded.SwapVert) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(Res.string.nodes_form_traffic_tracking),
                            style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = state.trackingActive, onCheckedChange = vm::setTracking)
                    }
                    OutlinedTextField(
                        value = state.multiplier, onValueChange = vm::onMultiplier,
                        label = { Text(stringResource(Res.string.nodes_form_multiplier)) },
                        placeholder = { Text("1.0") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (state.trackingActive) {
                        OutlinedTextField(
                            value = state.trafficLimitGb, onValueChange = vm::onTrafficLimitGb,
                            label = { Text(stringResource(Res.string.nodes_form_traffic_limit_gb)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.resetDay, onValueChange = vm::onResetDay,
                                label = { Text(stringResource(Res.string.nodes_form_reset_day)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = state.notifyPercent, onValueChange = vm::onNotifyPercent,
                                label = { Text(stringResource(Res.string.nodes_form_notify_at)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            item {
                FormCard(stringResource(Res.string.nodes_form_tags), Icons.Rounded.Sell) {
                    OutlinedTextField(
                        value = state.tags, onValueChange = vm::onTags,
                        label = { Text(stringResource(Res.string.nodes_form_tags)) },
                        placeholder = { Text(stringResource(Res.string.nodes_form_tags_hint)) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                state.actionError?.let {
                    Text(it.resolve(), color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp))
                }
                Button(
                    onClick = { vm.submit(onSaved) },
                    enabled = state.canSave,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(percent = 50),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp).heightIn(max = 18.dp),
                            strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(if (isEdit) Icons.Rounded.Check else Icons.Rounded.Add,
                            contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    }
                    Text(stringResource(if (isEdit) Res.string.nodes_form_save else Res.string.nodes_form_create))
                }
            }
        }
    }
}

@Composable
private fun FormCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.heightIn(max = 18.dp))
                Text(title, style = MaterialTheme.typography.titleSmall)
            }
            content()
        }
    }
}

@Composable
private fun CountryPickerDialog(onPick: (String) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) countries
        else countries.filter {
            it.name.contains(query, ignoreCase = true) || it.code.contains(query, ignoreCase = true)
        }
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp).heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    label = { Text(stringResource(Res.string.nodes_form_country_search)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(filtered, key = { it.code }) { c ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onPick(c.code) }.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(countryFlag(c.code))
                            Text("${c.name} (${c.code})", style = MaterialTheme.typography.bodyMedium)
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
