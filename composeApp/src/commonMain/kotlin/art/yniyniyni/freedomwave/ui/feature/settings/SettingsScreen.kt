@file:OptIn(ExperimentalTransitionApi::class)

package art.yniyniyni.freedomwave.ui.feature.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.ui.components.DetailRow
import art.yniyniyni.freedomwave.ui.components.DetailSectionTitle
import art.yniyniyni.freedomwave.ui.components.FwDetailCard
import art.yniyniyni.freedomwave.ui.components.FwSectionIcon
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_DARK
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_LIGHT
import art.yniyniyni.freedomwave.data.store.AppPreferences.Companion.THEME_SYSTEM
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_cancel
import freedomwave.composeapp.generated.resources.common_connect
import freedomwave.composeapp.generated.resources.common_empty_dash
import freedomwave.composeapp.generated.resources.settings_about
import freedomwave.composeapp.generated.resources.settings_api_key
import freedomwave.composeapp.generated.resources.settings_appearance
import freedomwave.composeapp.generated.resources.settings_biometric_lock
import freedomwave.composeapp.generated.resources.settings_biometric_on_desc
import freedomwave.composeapp.generated.resources.settings_biometric_unavailable
import freedomwave.composeapp.generated.resources.settings_change_key
import freedomwave.composeapp.generated.resources.settings_change_key_title
import freedomwave.composeapp.generated.resources.settings_connection
import freedomwave.composeapp.generated.resources.settings_copy_key
import freedomwave.composeapp.generated.resources.settings_geo_lookup
import freedomwave.composeapp.generated.resources.settings_geo_lookup_desc
import freedomwave.composeapp.generated.resources.settings_github
import freedomwave.composeapp.generated.resources.settings_language
import freedomwave.composeapp.generated.resources.settings_log_out
import freedomwave.composeapp.generated.resources.settings_oss_licenses
import freedomwave.composeapp.generated.resources.settings_oss_licenses_open
import freedomwave.composeapp.generated.resources.settings_privacy
import freedomwave.composeapp.generated.resources.settings_security
import freedomwave.composeapp.generated.resources.settings_server
import freedomwave.composeapp.generated.resources.settings_server_url
import freedomwave.composeapp.generated.resources.settings_theme_dark
import freedomwave.composeapp.generated.resources.settings_theme_light
import freedomwave.composeapp.generated.resources.settings_theme_system
import freedomwave.composeapp.generated.resources.settings_title
import freedomwave.composeapp.generated.resources.settings_version
import freedomwave.composeapp.generated.resources.squads_title
import freedomwave.composeapp.generated.resources.infra_title
import art.yniyniyni.freedomwave.ui.auth.rememberBiometricAuthenticator
import art.yniyniyni.freedomwave.ui.components.FwTopBar
import art.yniyniyni.freedomwave.ui.feature.infrabilling.InfraBillingScreen
import art.yniyniyni.freedomwave.ui.feature.squads.SquadsScreen
import art.yniyniyni.freedomwave.ui.navigation.BackGestureEffect
import art.yniyniyni.freedomwave.ui.l10n.AppLanguage
import art.yniyniyni.freedomwave.ui.l10n.applyAppLanguage
import art.yniyniyni.freedomwave.ui.l10n.UiText
import art.yniyniyni.freedomwave.ui.l10n.currentAppLanguageTag
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import art.yniyniyni.freedomwave.APP_VERSION
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val prefs: AppPreferences = koinInject()
    val serverUrl        by prefs.serverUrl.collectAsState("")
    val apiKey           by prefs.apiKey.collectAsState(null)
    val themeMode        by prefs.themeMode.collectAsState(THEME_SYSTEM)
    val biometricEnabled by prefs.biometricEnabled.collectAsState(false)
    val geoLookupEnabled by prefs.geoLookupEnabled.collectAsState(false)
    val clipboard        = LocalClipboardManager.current
    val uriHandler       = LocalUriHandler.current
    val biometricAuth    = rememberBiometricAuthenticator()
    val canUseBiometrics = biometricAuth.isAvailable()
    val monoFont         = LocalFwMonoFont.current
    val emptyDash        = stringResource(Res.string.common_empty_dash)

    val maskedKey = apiKey?.let {
        if (it.length > 8) it.take(8) + "•".repeat(16) else "•".repeat(it.length)
    } ?: emptyDash

    // Master/detail nav so the licenses sub-screen gets the same predictive-back animation
    // as the other detail screens (Users/Nodes/Hosts).
    var stack by remember { mutableStateOf(listOf<SettingsNav>(SettingsNav.Settings)) }
    val top = stack.last()
    val canGoBack = stack.size > 1

    val transitionState = remember { SeekableTransitionState<SettingsNav>(SettingsNav.Settings) }
    val transition = rememberTransition(transitionState, label = "settings_nav")
    LaunchedEffect(top) {
        if (transitionState.currentState != top) transitionState.animateTo(top)
    }
    BackGestureEffect(
        enabled = canGoBack,
        onProgress = { fraction -> transitionState.seekTo(fraction, stack[stack.size - 2]) },
        onCommit   = {
            val target = stack[stack.size - 2]
            transitionState.animateTo(target)
            stack = stack.dropLast(1)
        },
        onCancel   = { transitionState.animateTo(top) },
    )

    transition.AnimatedContent(
        contentKey = { it.key },
        transitionSpec = {
            val deeper = targetState.depth > initialState.depth
            if (deeper) {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 4 }
            } else {
                slideInHorizontally { -it / 4 } togetherWith slideOutHorizontally { it }
            }.apply { targetContentZIndex = if (deeper) 1f else 0f }
        },
    ) { navEntry ->
        if (navEntry == SettingsNav.Licenses) {
            LicensesScreen(onBack = { stack = stack.dropLast(1) })
            return@AnimatedContent
        }

        if (navEntry == SettingsNav.Squads) {
            SquadsScreen(onBack = { stack = stack.dropLast(1) })
            return@AnimatedContent
        }

        if (navEntry == SettingsNav.InfraBilling) {
            InfraBillingScreen(onBack = { stack = stack.dropLast(1) })
            return@AnimatedContent
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
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.settings_connection), Icons.Rounded.Cloud)
                    Spacer(Modifier.height(4.dp))
                    DetailRow(stringResource(Res.string.settings_server), serverUrl.ifBlank { emptyDash }, monoFont)
                    DetailRow(stringResource(Res.string.settings_api_key), maskedKey, monoFont)
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
                FwDetailCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { stack = stack + SettingsNav.Squads },
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FwSectionIcon(Icons.Rounded.Groups, MaterialTheme.colorScheme.tertiary)
                        Text(
                            stringResource(Res.string.squads_title),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                FwDetailCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { stack = stack + SettingsNav.InfraBilling },
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FwSectionIcon(Icons.AutoMirrored.Rounded.ReceiptLong, MaterialTheme.colorScheme.secondary)
                        Text(
                            stringResource(Res.string.infra_title),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.settings_appearance), Icons.Rounded.Palette)
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
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.settings_language), Icons.Rounded.Translate, MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.height(8.dp))
                    var current by remember { mutableStateOf(AppLanguage.fromTag(currentAppLanguageTag())) }
                    val languages = remember { AppLanguage.entries }
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        languages.forEachIndexed { i, lang ->
                            SegmentedButton(
                                shape    = SegmentedButtonDefaults.itemShape(index = i, count = languages.size),
                                onClick  = { current = lang; applyAppLanguage(lang.tag) },
                                selected = current == lang
                            ) { Text(stringResource(lang.labelRes)) }
                        }
                    }
                }
            }

            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.settings_security), Icons.Rounded.Fingerprint, MaterialTheme.colorScheme.secondary)
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
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.settings_privacy), Icons.Rounded.Shield)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(Res.string.settings_geo_lookup), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                stringResource(Res.string.settings_geo_lookup_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = geoLookupEnabled,
                            onCheckedChange = { vm.setGeoLookupEnabled(it) }
                        )
                    }
                }
            }

            item {
                FwDetailCard {
                    DetailSectionTitle(stringResource(Res.string.settings_about), Icons.Rounded.Info, MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.height(8.dp))
                    DetailRow(stringResource(Res.string.settings_version), APP_VERSION, monoFont)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(GITHUB_URL) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(Res.string.settings_github), style = MaterialTheme.typography.bodyMedium)
                        Icon(
                            Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = stringResource(Res.string.settings_oss_licenses_open),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { stack = stack + SettingsNav.Licenses },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(Res.string.settings_oss_licenses), style = MaterialTheme.typography.bodyMedium)
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = stringResource(Res.string.settings_oss_licenses_open),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

    // Dialog rendered outside AnimatedContent so it is not recreated on every content transition
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
}

private const val GITHUB_URL = "https://github.com/yniyniyni/freedomwave"

/** Two-level nav inside the Settings tab so the licenses sub-screen animates like other detail screens. */
private sealed interface SettingsNav {
    data object Settings : SettingsNav
    data object Licenses : SettingsNav
    data object Squads : SettingsNav
    data object InfraBilling : SettingsNav

    val depth: Int get() = if (this is Settings) 0 else 1
    val key: String get() = when (this) {
        Settings -> "settings"
        Licenses -> "licenses"
        Squads -> "squads"
        InfraBilling -> "infra_billing"
    }
}

@Composable
private fun ChangeKeyDialog(
    serverUrl: String,
    apiKey: String,
    isLoading: Boolean,
    error: UiText?,
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
                    Text(it.resolve(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
