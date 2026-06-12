package art.yniyniyni.freedomwave.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppPreferences(
    private val dataStore: DataStore<Preferences>,
    private val secretStore: SecretStore
) {

    companion object {
        private val KEY_SERVER_URL  = stringPreferencesKey("server_url")
        private val KEY_API_KEY     = stringPreferencesKey("api_key")
        private val KEY_THEME_MODE  = stringPreferencesKey("theme_mode")
        private val KEY_BIOMETRIC   = booleanPreferencesKey("biometric_enabled")
        private val KEY_GEO_LOOKUP  = booleanPreferencesKey("geo_lookup_enabled")

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT  = "light"
        const val THEME_DARK   = "dark"
    }

    val serverUrl:       Flow<String>  = dataStore.data.map { it[KEY_SERVER_URL] ?: "" }
    // The stored value is ciphertext; decrypt for display/copy. A null decrypt means a
    // legacy plaintext key (pre-encryption) — surface it as-is until getApiKey() migrates it.
    val apiKey:          Flow<String?> = dataStore.data.map { p ->
        p[KEY_API_KEY]?.let { secretStore.decrypt(it) ?: it }
    }
    val isLoggedIn:      Flow<Boolean> = dataStore.data.map { !it[KEY_API_KEY].isNullOrEmpty() }
    val themeMode:       Flow<String>  = dataStore.data.map { it[KEY_THEME_MODE] ?: THEME_SYSTEM }
    val biometricEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_BIOMETRIC] ?: false }
    // Off by default: when off, client IPs are never sent to the third-party ipwho.is service.
    val geoLookupEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_GEO_LOOKUP] ?: false }

    suspend fun getServerUrl(): String  = dataStore.data.first()[KEY_SERVER_URL] ?: ""
    suspend fun getGeoLookupEnabled(): Boolean = dataStore.data.first()[KEY_GEO_LOOKUP] ?: false
    suspend fun getThemeMode(): String  = dataStore.data.first()[KEY_THEME_MODE] ?: THEME_SYSTEM

    suspend fun getApiKey(): String? {
        val stored = dataStore.data.first()[KEY_API_KEY] ?: return null
        secretStore.decrypt(stored)?.let { return it }
        // Legacy plaintext written before encryption existed: re-encrypt in place so the
        // plaintext no longer sits in the DataStore, then return the (still valid) key.
        val token = secretStore.encrypt(stored)
        if (token != stored) dataStore.edit { it[KEY_API_KEY] = token }
        return stored
    }

    suspend fun saveApiKey(serverUrl: String, apiKey: String) {
        val token = secretStore.encrypt(apiKey.trim())
        dataStore.edit {
            it[KEY_SERVER_URL] = serverUrl.trimEnd('/')
            it[KEY_API_KEY]    = token
        }
    }

    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun saveBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_BIOMETRIC] = enabled }
    }

    suspend fun saveGeoLookupEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_GEO_LOOKUP] = enabled }
    }

    suspend fun clearCredentials() {
        dataStore.edit { it.remove(KEY_API_KEY) }
    }
}
