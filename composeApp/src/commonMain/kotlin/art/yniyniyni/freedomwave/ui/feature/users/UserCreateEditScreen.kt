package art.yniyniyni.freedomwave.ui.feature.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private val STRATEGIES = listOf("NO_RESET", "DAY", "WEEK", "MONTH")
private val STRATEGY_LABELS = mapOf(
    "NO_RESET" to "No reset",
    "DAY"      to "Day",
    "WEEK"     to "Week",
    "MONTH"    to "Month"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserCreateEditScreen(
    state: UsersUiState,
    vm: UsersViewModel
) {
    val isCreate = state.editingUser == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCreate) "New User" else "Edit ${state.editingUser!!.username}") },
                navigationIcon = { TextButton(onClick = vm::closeForm) { Text("← Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Identity", style = MaterialTheme.typography.titleSmall)

                        OutlinedTextField(
                            value = if (isCreate) state.formUsername else state.editingUser!!.username,
                            onValueChange = if (isCreate) vm::onFormUsername else { _ -> },
                            label = { Text(if (isCreate) "Username *" else "Username") },
                            singleLine = true,
                            enabled = isCreate && !state.formIsLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.formTag,
                            onValueChange = vm::onFormTag,
                            label = { Text("Tag") },
                            singleLine = true,
                            enabled = !state.formIsLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.formEmail,
                            onValueChange = vm::onFormEmail,
                            label = { Text("Email") },
                            singleLine = true,
                            enabled = !state.formIsLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.formDescription,
                            onValueChange = vm::onFormDescription,
                            label = { Text("Description") },
                            enabled = !state.formIsLoading,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Traffic", style = MaterialTheme.typography.titleSmall)

                        OutlinedTextField(
                            value = state.formTrafficGb,
                            onValueChange = vm::onFormTrafficGb,
                            label = { Text("Limit (GB)") },
                            supportingText = { Text("0 = unlimited") },
                            singleLine = true,
                            enabled = !state.formIsLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            "Reset strategy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            STRATEGIES.forEachIndexed { index, opt ->
                                SegmentedButton(
                                    selected = state.formStrategy == opt,
                                    onClick = { vm.onFormStrategy(opt) },
                                    shape = SegmentedButtonDefaults.itemShape(index, STRATEGIES.size),
                                    enabled = !state.formIsLoading,
                                    label = { Text(STRATEGY_LABELS[opt] ?: opt) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Expiry", style = MaterialTheme.typography.titleSmall)

                        OutlinedTextField(
                            value = state.formExpireDate,
                            onValueChange = vm::onFormExpireDate,
                            label = { Text("Expiry date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            supportingText = { Text("Leave blank for no expiry") },
                            singleLine = true,
                            enabled = !state.formIsLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (state.formError != null) {
                item {
                    Text(
                        state.formError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = vm::saveForm,
                    enabled = !state.formIsLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.formIsLoading) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (isCreate) "Create" else "Save changes")
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
