package art.yniyniyni.freedomwave.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val KEY_SERVER_URL  = stringPreferencesKey("server_url")
        private val KEY_API_KEY     = stringPreferencesKey("api_key")
        private val KEY_THEME_MODE  = stringPreferencesKey("theme_mode")
        private val KEY_BIOMETRIC   = booleanPreferencesKey("biometric_enabled")

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT  = "light"
        const val THEME_DARK   = "dark"
    }

    val serverUrl:       Flow<String>  = dataStore.data.map { it[KEY_SERVER_URL] ?: "" }
    val apiKey:          Flow<String?> = dataStore.data.map { it[KEY_API_KEY] }
    val isLoggedIn:      Flow<Boolean> = apiKey.map { !it.isNullOrEmpty() }
    val themeMode:       Flow<String>  = dataStore.data.map { it[KEY_THEME_MODE] ?: THEME_SYSTEM }
    val biometricEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_BIOMETRIC] ?: false }

    suspend fun getServerUrl(): String  = dataStore.data.first()[KEY_SERVER_URL] ?: ""
    suspend fun getApiKey(): String?    = dataStore.data.first()[KEY_API_KEY]
    suspend fun getThemeMode(): String  = dataStore.data.first()[KEY_THEME_MODE] ?: THEME_SYSTEM

    suspend fun saveApiKey(serverUrl: String, apiKey: String) {
        dataStore.edit {
            it[KEY_SERVER_URL] = serverUrl.trimEnd('/')
            it[KEY_API_KEY]    = apiKey.trim()
        }
    }

    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun saveBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_BIOMETRIC] = enabled }
    }

    suspend fun clearCredentials() {
        dataStore.edit { it.remove(KEY_API_KEY) }
    }
}
