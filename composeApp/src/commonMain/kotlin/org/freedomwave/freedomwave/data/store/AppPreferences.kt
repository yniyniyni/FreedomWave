package org.freedomwave.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import org.freedomwave.data.api.PanelVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

class AppPreferences(
    private val dataStore: DataStore<Preferences>,
    private val secretStore: SecretStore
) {

    @Volatile
    private var cachedServerUrl: String? = null

    // Read on every user-route build, so cache it like the server URL rather than hitting
    // DataStore each time.
    @Volatile
    private var cachedPanelVersion: PanelVersion? = null

    private val migrationMutex = Mutex()

    companion object {
        private val KEY_SERVER_URL  = stringPreferencesKey("server_url")
        private val KEY_API_KEY     = stringPreferencesKey("api_key")
        private val KEY_THEME_MODE  = stringPreferencesKey("theme_mode")
        private val KEY_BIOMETRIC   = booleanPreferencesKey("biometric_enabled")
        private val KEY_GEO_LOOKUP  = booleanPreferencesKey("geo_lookup_enabled")
        // Raw version string from GET /api/system/stats/recap, captured at login. Decides
        // whether user routes are keyed by numeric id (3.x) or uuid (2.8.x).
        private val KEY_PANEL_VERSION = stringPreferencesKey("panel_version")

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

    suspend fun getServerUrl(): String {
        cachedServerUrl?.let { return it }
        val url = dataStore.data.first()[KEY_SERVER_URL] ?: ""
        cachedServerUrl = url
        return url
    }
    /**
     * Panel version captured at login. Absent (fresh install, or logged out) yields
     * [PanelVersion.UNKNOWN], which speaks the 3.x contract — see that field for why.
     */
    suspend fun getPanelVersion(): PanelVersion {
        cachedPanelVersion?.let { return it }
        val stored = dataStore.data.first()[KEY_PANEL_VERSION]
        val version = stored?.let { PanelVersion.parse(it) } ?: PanelVersion.UNKNOWN
        cachedPanelVersion = version
        return version
    }

    suspend fun savePanelVersion(version: PanelVersion) {
        // The dashboard re-reports this on every refresh; skip the disk write when nothing moved.
        if (cachedPanelVersion == version) return
        cachedPanelVersion = version // warm cache immediately
        dataStore.edit { it[KEY_PANEL_VERSION] = version.raw }
    }

    suspend fun getGeoLookupEnabled(): Boolean = dataStore.data.first()[KEY_GEO_LOOKUP] ?: false
    suspend fun getThemeMode(): String  = dataStore.data.first()[KEY_THEME_MODE] ?: THEME_SYSTEM

    suspend fun getApiKey(): String? {
        val stored = dataStore.data.first()[KEY_API_KEY] ?: return null
        secretStore.decrypt(stored)?.let { return it }
        
        // Legacy plaintext migration with double-checked locking
        return migrationMutex.withLock {
            val refreshed = dataStore.data.first()[KEY_API_KEY] ?: return null
            secretStore.decrypt(refreshed)?.let { return it }
            
            val token = secretStore.encrypt(stored)
            if (token != stored) dataStore.edit { it[KEY_API_KEY] = token }
            stored
        }
    }

    suspend fun saveApiKey(serverUrl: String, apiKey: String) {
        cachedServerUrl = serverUrl.trimEnd('/') // warm cache immediately
        val token = secretStore.encrypt(apiKey.trim())
        dataStore.edit {
            it[KEY_SERVER_URL] = cachedServerUrl!!
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
        cachedServerUrl = null
        // Drop the version too: the next login may point at a different panel, and a stale
        // 2.x reading against a 3.x server would break every user route.
        cachedPanelVersion = null
        dataStore.edit {
            it.remove(KEY_API_KEY)
            it.remove(KEY_PANEL_VERSION)
        }
    }
}
