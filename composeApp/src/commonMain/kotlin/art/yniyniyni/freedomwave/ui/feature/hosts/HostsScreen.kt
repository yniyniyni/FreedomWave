package art.yniyniyni.freedomwave.ui.feature.hosts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.data.api.dto.HostDto
import art.yniyniyni.freedomwave.data.api.service.HostService
import art.yniyniyni.freedomwave.data.api.ApiError
import art.yniyniyni.freedomwave.data.store.AppPreferences
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostsScreen() {
    val hostService: HostService = koinInject()
    val prefs: AppPreferences = koinInject()
    val scope = rememberCoroutineScope()

    var hosts by remember { mutableStateOf<List<HostDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            isLoading = true; error = null
            runCatching { hostService.getHosts(prefs.getServerUrl()).response }
                .onSuccess { hosts = it; isLoading = false }
                .onFailure { e ->
                    isLoading = false; error = e.message
                    if (e is ApiError.Unauthorized) prefs.clearCredentials()
                }
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hosts (${hosts.size})") },
                actions = { TextButton(onClick = ::load) { Text("Refresh") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading && hosts.isEmpty() -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null && hosts.isEmpty() ->
                    Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = ::load, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
                    }
                else ->
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(hosts, key = { it.uuid }) { host ->
                            HostItem(host)
                        }
                    }
            }
        }
    }
}

@Composable
private fun HostItem(host: HostDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(host.remark, fontWeight = FontWeight.Medium)
                if (host.isDisabled) {
                    Text("Disabled", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                "${host.address}:${host.port}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(host.securityLayer, style = MaterialTheme.typography.labelSmall)
                host.tag?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
            }
        }
    }
}
