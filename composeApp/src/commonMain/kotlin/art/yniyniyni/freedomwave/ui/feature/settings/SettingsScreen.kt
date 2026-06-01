package art.yniyniyni.freedomwave.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_DARK
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_LIGHT
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_SYSTEM
import art.yniyniyni.freedomwave.ui.auth.rememberBiometricAuthenticator
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
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Connection", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                        InfoRow("Server", serverUrl.ifBlank { "—" })
                        InfoRow("API Key", maskedKey)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { apiKey?.let { clipboard.setText(AnnotatedString(it)) } },
                                enabled = apiKey != null,
                                modifier = Modifier.weight(1f)
                            ) { Text("Copy key") }

                            OutlinedButton(
                                onClick = vm::openChangeKeyDialog,
                                modifier = Modifier.weight(1f)
                            ) { Text("Change key") }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Appearance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                        val modes = listOf(THEME_SYSTEM, THEME_LIGHT, THEME_DARK)
                        val labels = listOf("System", "Light", "Dark")

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            modes.forEachIndexed { i, mode ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(index = i, count = modes.size),
                                    onClick = { vm.setThemeMode(mode) },
                                    selected = themeMode == mode
                                ) { Text(labels[i]) }
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Security", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Biometric lock", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    if (canUseBiometrics) "Require authentication on launch"
                                    else "No biometrics enrolled on this device",
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
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("About", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        InfoRow("Version", APP_VERSION)
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = vm::logout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Log Out") }
            }
        }
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
        title = { Text("Change API Key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = onServerUrlChange,
                    label = { Text("Server URL") },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = onApiKeyChange,
                    label = { Text("API Key") },
                    singleLine = true,
                    enabled = !isLoading,
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
            Button(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                else Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancel") }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, maxLines = 1)
    }
}
