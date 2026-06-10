package art.yniyniyni.freedomwave.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import art.yniyniyni.freedomwave.ui.components.FwTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_DARK
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_LIGHT
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_SYSTEM
import art.yniyniyni.freedomwave.ui.auth.rememberBiometricAuthenticator
import art.yniyniyni.freedomwave.ui.l10n.AppLanguage
import art.yniyniyni.freedomwave.ui.l10n.applyAppLanguage
import art.yniyniyni.freedomwave.ui.l10n.currentAppLanguageTag
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.common_cancel
import art.yniyniyni.freedomwave.resources.common_connect
import art.yniyniyni.freedomwave.resources.settings_about
import art.yniyniyni.freedomwave.resources.settings_api_key
import art.yniyniyni.freedomwave.resources.settings_appearance
import art.yniyniyni.freedomwave.resources.settings_biometric_lock
import art.yniyniyni.freedomwave.resources.settings_biometric_on_desc
import art.yniyniyni.freedomwave.resources.settings_biometric_unavailable
import art.yniyniyni.freedomwave.resources.settings_change_key
import art.yniyniyni.freedomwave.resources.settings_change_key_title
import art.yniyniyni.freedomwave.resources.settings_connection
import art.yniyniyni.freedomwave.resources.settings_copy_key
import art.yniyniyni.freedomwave.resources.settings_language
import art.yniyniyni.freedomwave.resources.settings_log_out
import art.yniyniyni.freedomwave.resources.settings_security
import art.yniyniyni.freedomwave.resources.settings_server
import art.yniyniyni.freedomwave.resources.settings_server_url
import art.yniyniyni.freedomwave.resources.settings_theme_dark
import art.yniyniyni.freedomwave.resources.settings_theme_light
import art.yniyniyni.freedomwave.resources.settings_theme_system
import art.yniyniyni.freedomwave.resources.settings_title
import art.yniyniyni.freedomwave.resources.settings_version
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val APP_VERSION = "1.0.0"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val prefs: AppPreferences = koinInject()
    val serverUrl        by prefs.serverUrl.collectAsState("")
    val apiKey           by prefs.apiKey.collectAsState(null)
    val themeMode        by prefs.themeMode.collectAsState(THEME_SYSTEM)
    val biometricEnabled by prefs.biometricEnabled.collectAsState(false)
    val clipboard        = LocalClipboardManager.current
    val biometricAuth    = rememberBiometricAuthenticator()
    val canUseBiometrics = biometricAuth.isAvailable()
    val monoFont         = LocalFwMonoFont.current

    val maskedKey = apiKey?.let {
        if (it.length > 8) it.take(8) + "•".repeat(16) else "•".repeat(it.length)
    } ?: "—"

    if (state.showChangeKeyDialog) {
        ChangeKeyDialog(
            serverUrl = state.dialogServerUrl,
            apiKey = state.dialogApiKey,
            isLoading = state.dialogIsLoading,
            error = state.dialogError,
            onServerUrlChange = vm::onDialogServerUrlChange,
            onApiKeyChange = vm::onDialogApiKeyChange,
            onConfirm = vm::saveApiKey,
            onDismiss = vm::dismissChangeKeyDialog
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { FwTopBar(title = stringResource(Res.string.settings_title)) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FwCard {
                    Text(stringResource(Res.string.settings_connection), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    InfoRow(stringResource(Res.string.settings_server), serverUrl.ifBlank { "—" }, monoFont)
                    InfoRow(stringResource(Res.string.settings_api_key), maskedKey, monoFont)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { apiKey?.let { clipboard.setText(AnnotatedString(it)) } },
                            enabled = apiKey != null,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(percent = 50),
                        ) { Text(stringResource(Res.string.settings_copy_key)) }
                        OutlinedButton(
                            onClick = vm::openChangeKeyDialog,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(percent = 50),
                        ) { Text(stringResource(Res.string.settings_change_key)) }
                    }
                }
            }

            item {
                FwCard {
                    Text(stringResource(Res.string.settings_appearance), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    val modes  = listOf(THEME_SYSTEM, THEME_LIGHT, THEME_DARK)
                    val labels = listOf(
                        stringResource(Res.string.settings_theme_system),
                        stringResource(Res.string.settings_theme_light),
                        stringResource(Res.string.settings_theme_dark)
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        modes.forEachIndexed { i, mode ->
                            SegmentedButton(
                                shape    = SegmentedButtonDefaults.itemShape(index = i, count = modes.size),
                                onClick  = { vm.setThemeMode(mode) },
                                selected = themeMode == mode
                            ) { Text(labels[i]) }
                        }
                    }
                }
            }

            item {
                FwCard {
                    Text(stringResource(Res.string.settings_language), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    val current = AppLanguage.fromTag(currentAppLanguageTag())
                    val languages = AppLanguage.entries
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        languages.forEachIndexed { i, lang ->
                            SegmentedButton(
                                shape    = SegmentedButtonDefaults.itemShape(index = i, count = languages.size),
                                onClick  = { applyAppLanguage(lang.tag) },
                                selected = current == lang
                            ) { Text(stringResource(lang.labelRes)) }
                        }
                    }
                }
            }

            item {
                FwCard {
                    Text(stringResource(Res.string.settings_security), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(Res.string.settings_biometric_lock), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (canUseBiometrics) stringResource(Res.string.settings_biometric_on_desc)
                                else stringResource(Res.string.settings_biometric_unavailable),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = biometricEnabled && canUseBiometrics,
                            onCheckedChange = { vm.setBiometricEnabled(it) },
                            enabled = canUseBiometrics
                        )
                    }
                }
            }

            item {
                FwCard {
                    Text(stringResource(Res.string.settings_about), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    InfoRow(stringResource(Res.string.settings_version), APP_VERSION, monoFont)
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = vm::logout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(Res.string.settings_log_out)) }
            }
        }
    }
}

@Composable
private fun FwCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = { content() },
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, monoFont: androidx.compose.ui.text.font.FontFamily) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoFont), maxLines = 1)
    }
}

@Composable
private fun ChangeKeyDialog(
    serverUrl: String,
    apiKey: String,
    isLoading: Boolean,
    error: String?,
    onServerUrlChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(stringResource(Res.string.settings_change_key_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = onServerUrlChange,
                    label = { Text(stringResource(Res.string.settings_server_url)) },
                    singleLine = true,
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = onApiKeyChange,
                    label = { Text(stringResource(Res.string.settings_api_key)) },
                    singleLine = true,
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.medium,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                shape = RoundedCornerShape(percent = 50),
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(Res.string.common_connect))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(Res.string.common_cancel)) }
        }
    )
}
