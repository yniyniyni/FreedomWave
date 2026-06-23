package art.yniyniyni.freedomwave.ui.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.ui.l10n.resolve
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.login_api_key
import freedomwave.composeapp.generated.resources.login_api_key_hint
import freedomwave.composeapp.generated.resources.login_server_url
import freedomwave.composeapp.generated.resources.login_server_url_placeholder
import org.jetbrains.compose.resources.stringResource

/**
 * The login form fields (server URL + API key + hint + error), bound to [LoginViewModel].
 * The submit button is supplied by the caller — the welcome carousel keeps a fixed Connect
 * button in its bottom bar — so this composable is just the scrollable input content.
 */
@Composable
fun LoginFields(
    vm: LoginViewModel,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.serverUrl,
            onValueChange = vm::onServerUrlChange,
            label = { Text(stringResource(Res.string.login_server_url)) },
            placeholder = { Text(stringResource(Res.string.login_server_url_placeholder)) },
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )

        OutlinedTextField(
            value = state.apiKey,
            onValueChange = vm::onApiKeyChange,
            label = { Text(stringResource(Res.string.login_api_key)) },
            shape = MaterialTheme.shapes.medium,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { vm.save() }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )

        Text(
            text = stringResource(Res.string.login_api_key_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )

        state.error?.let { error ->
            Text(
                text = error.resolve(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
