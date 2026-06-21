package art.yniyniyni.freedomwave.ui.feature.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.window.Dialog
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_cancel
import freedomwave.composeapp.generated.resources.common_create
import freedomwave.composeapp.generated.resources.users_blank_zero_unlimited
import freedomwave.composeapp.generated.resources.users_date
import freedomwave.composeapp.generated.resources.users_detail_devices
import freedomwave.composeapp.generated.resources.users_detail_email
import freedomwave.composeapp.generated.resources.users_detail_squads
import freedomwave.composeapp.generated.resources.users_detail_tag
import freedomwave.composeapp.generated.resources.users_detail_traffic
import freedomwave.composeapp.generated.resources.users_edit_title
import freedomwave.composeapp.generated.resources.users_form_access
import freedomwave.composeapp.generated.resources.users_form_description
import freedomwave.composeapp.generated.resources.users_form_enabled
import freedomwave.composeapp.generated.resources.users_form_enabled_desc
import freedomwave.composeapp.generated.resources.users_form_expiry
import freedomwave.composeapp.generated.resources.users_form_identity
import freedomwave.composeapp.generated.resources.users_hwid_limit
import freedomwave.composeapp.generated.resources.users_limit_gb
import freedomwave.composeapp.generated.resources.users_new_user
import freedomwave.composeapp.generated.resources.common_ok
import freedomwave.composeapp.generated.resources.users_reset_strategy
import freedomwave.composeapp.generated.resources.users_save_changes
import freedomwave.composeapp.generated.resources.users_time
import freedomwave.composeapp.generated.resources.users_username
import freedomwave.composeapp.generated.resources.users_username_required
import freedomwave.composeapp.generated.resources.users_zero_unlimited
import art.yniyniyni.freedomwave.ui.components.FwDetailTopBar
import art.yniyniyni.freedomwave.ui.components.FwSectionIcon
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.util.ExpiryPreset
import art.yniyniyni.freedomwave.util.presetExpiryMillis
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

private val STRATEGIES = listOf("NO_RESET", "DAY", "WEEK", "MONTH", "MONTH_ROLLING")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun UserCreateEditScreen(
    state: UsersUiState,
    vm: UsersViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val isCreate = state.editingUser == null

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            FwDetailTopBar(
                title = if (isCreate) stringResource(Res.string.users_new_user)
                    else stringResource(Res.string.users_edit_title, state.editingUser!!.username),
                onBack = onBack,
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Identity
            item {
                FormCard(stringResource(Res.string.users_form_identity), Icons.Rounded.Person) {
                    OutlinedTextField(
                        value = state.formUsername,
                        onValueChange = if (isCreate) vm::onFormUsername else { _ -> },
                        label = {
                            Text(
                                stringResource(
                                    if (isCreate) Res.string.users_username_required else Res.string.users_username
                                )
                            )
                        },
                        singleLine = true,
                        enabled = isCreate && !state.formIsLoading,
                        isError = state.usernameError != null,
                        supportingText = state.usernameError?.let { { Text(it.resolve()) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.formTag,
                        onValueChange = vm::onFormTag,
                        label = { Text(stringResource(Res.string.users_detail_tag)) },
                        singleLine = true,
                        enabled = !state.formIsLoading,
                        isError = state.tagError != null,
                        supportingText = state.tagError?.let { { Text(it.resolve()) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.formEmail,
                        onValueChange = vm::onFormEmail,
                        label = { Text(stringResource(Res.string.users_detail_email)) },
                        singleLine = true,
                        enabled = !state.formIsLoading,
                        isError = state.emailError != null,
                        supportingText = state.emailError?.let { { Text(it.resolve()) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.formDescription,
                        onValueChange = vm::onFormDescription,
                        label = { Text(stringResource(Res.string.users_form_description)) },
                        enabled = !state.formIsLoading,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Access
            item {
                FormCard(stringResource(Res.string.users_form_access), Icons.Rounded.Key, MaterialTheme.colorScheme.tertiary) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(Res.string.users_form_enabled), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                stringResource(Res.string.users_form_enabled_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = state.formStatusEnabled,
                            onCheckedChange = vm::onFormStatusEnabled,
                            enabled = !state.formIsLoading,
                        )
                    }
                }
            }

            // Traffic
            item {
                FormCard(stringResource(Res.string.users_detail_traffic), Icons.Rounded.DataUsage, MaterialTheme.colorScheme.secondary) {
                    OutlinedTextField(
                        value = state.formTrafficGb,
                        onValueChange = vm::onFormTrafficGb,
                        label = { Text(stringResource(Res.string.users_limit_gb)) },
                        supportingText = { Text(stringResource(Res.string.users_zero_unlimited)) },
                        singleLine = true,
                        enabled = !state.formIsLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    StrategyDropdown(
                        selected = state.formStrategy,
                        enabled = !state.formIsLoading,
                        onSelect = vm::onFormStrategy,
                    )
                }
            }

            // Expiry
            item {
                FormCard(stringResource(Res.string.users_form_expiry), Icons.Rounded.Schedule, MaterialTheme.colorScheme.primary) {
                    ExpiryEditor(
                        expireMillis = state.formExpireMillis,
                        enabled = !state.formIsLoading,
                        onChange = vm::onFormExpireMillis,
                    )
                }
            }

            // Devices
            item {
                FormCard(stringResource(Res.string.users_detail_devices), Icons.Rounded.Devices, MaterialTheme.colorScheme.tertiary) {
                    OutlinedTextField(
                        value = state.formHwid,
                        onValueChange = vm::onFormHwid,
                        label = { Text(stringResource(Res.string.users_hwid_limit)) },
                        supportingText = { Text(stringResource(Res.string.users_blank_zero_unlimited)) },
                        singleLine = true,
                        enabled = !state.formIsLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Squads
            if (state.formSquads.isNotEmpty()) {
                item {
                    FormCard(stringResource(Res.string.users_detail_squads), Icons.Rounded.Groups, MaterialTheme.colorScheme.secondary) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.formSquads.forEach { squad ->
                                FilterChip(
                                    selected = squad.uuid in state.formSelectedSquadUuids,
                                    onClick = { vm.onFormSquadToggle(squad.uuid) },
                                    enabled = !state.formIsLoading,
                                    label = { Text(squad.name) },
                                )
                            }
                        }
                    }
                }
            }

            state.formError?.let { formError ->
                item {
                    Text(
                        formError.resolve(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = { vm.saveForm(onSuccess = onSaved) },
                    enabled = !state.formIsLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(percent = 50),
                ) {
                    if (state.formIsLoading) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            stringResource(
                                if (isCreate) Res.string.common_create else Res.string.users_save_changes
                            )
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun FormCard(
    title: String,
    icon: ImageVector? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (icon != null) {
                    FwSectionIcon(icon, tint)
                }
                Text(title, style = MaterialTheme.typography.titleSmall)
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrategyDropdown(selected: String, enabled: Boolean, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        OutlinedTextField(
            value = trafficStrategyLabel(selected),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(Res.string.users_reset_strategy)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            STRATEGIES.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(trafficStrategyLabel(opt)) },
                    onClick = { onSelect(opt); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ExpiryEditor(expireMillis: Long, enabled: Boolean, onChange: (Long) -> Unit) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    val tz = TimeZone.currentSystemDefault()
    val ldt = remember(expireMillis) {
        Instant.fromEpochMilliseconds(expireMillis).toLocalDateTime(tz)
    }
    fun p2(n: Int) = n.toString().padStart(2, '0')
    val dateStr = "${ldt.year}-${p2(ldt.monthNumber)}-${p2(ldt.dayOfMonth)}"
    val timeStr = "${p2(ldt.hour)}:${p2(ldt.minute)}"

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = dateStr, onValueChange = {}, readOnly = true, enabled = enabled,
                label = { Text(stringResource(Res.string.users_date)) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (enabled) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { showDate = true }
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = timeStr, onValueChange = {}, readOnly = true, enabled = enabled,
                label = { Text(stringResource(Res.string.users_time)) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (enabled) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { showTime = true }
                )
            }
        }
    }

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ExpiryPreset.entries.forEach { preset ->
            OutlinedButton(
                onClick = { onChange(presetExpiryMillis(preset)) },
                enabled = enabled,
                shape = RoundedCornerShape(percent = 50),
            ) { Text(preset.label()) }
        }
    }

    if (showDate) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = expireMillis)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { picked ->
                        // DatePicker reports the selected day at UTC midnight; keep current time-of-day.
                        val newDate = Instant.fromEpochMilliseconds(picked)
                            .toLocalDateTime(TimeZone.UTC).date
                        val merged = LocalDateTime(
                            newDate.year, newDate.monthNumber, newDate.dayOfMonth, ldt.hour, ldt.minute,
                        )
                        onChange(merged.toInstant(tz).toEpochMilliseconds())
                    }
                    showDate = false
                }) { Text(stringResource(Res.string.common_ok)) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text(stringResource(Res.string.common_cancel)) } },
        ) { DatePicker(state = pickerState) }
    }

    if (showTime) {
        val timeState = rememberTimePickerState(
            initialHour = ldt.hour, initialMinute = ldt.minute, is24Hour = true,
        )
        Dialog(onDismissRequest = { showTime = false }) {
            Card(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TimePicker(state = timeState)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showTime = false }) { Text(stringResource(Res.string.common_cancel)) }
                        TextButton(onClick = {
                            val merged = LocalDateTime(
                                ldt.year, ldt.monthNumber, ldt.dayOfMonth, timeState.hour, timeState.minute,
                            )
                            onChange(merged.toInstant(tz).toEpochMilliseconds())
                            showTime = false
                        }) { Text(stringResource(Res.string.common_ok)) }
                    }
                }
            }
        }
    }
}

