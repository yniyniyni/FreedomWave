package art.yniyniyni.freedomwave.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_TOKEN      = stringPreferencesKey("auth_token")
    }

    val serverUrl: Flow<String> = dataStore.data.map { it[KEY_SERVER_URL] ?: "" }
    val token: Flow<String?>    = dataStore.data.map { it[KEY_TOKEN] }
    val isLoggedIn: Flow<Boolean> = token.map { !it.isNullOrEmpty() }

    suspend fun getServerUrl(): String = dataStore.data.first()[KEY_SERVER_URL] ?: ""
    suspend fun getToken(): String?    = dataStore.data.first()[KEY_TOKEN]

    suspend fun saveCredentials(serverUrl: String, token: String) {
        dataStore.edit {
            it[KEY_SERVER_URL] = serverUrl.trimEnd('/')
            it[KEY_TOKEN]      = token
        }
    }

    suspend fun clearCredentials() {
        dataStore.edit {
            it.remove(KEY_TOKEN)
        }
    }
}
