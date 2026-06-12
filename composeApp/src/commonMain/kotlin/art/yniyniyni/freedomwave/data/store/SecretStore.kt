package art.yniyniyni.freedomwave.data.store

/**
 * Platform secret codec for the panel API key (a long-lived admin JWT).
 *
 * The ciphertext lives in the app's DataStore; the encryption key never leaves the
 * platform keystore:
 *  - Android: a non-exportable AES/GCM key in AndroidKeyStore.
 *  - iOS: currently a pass-through stub (TODO: Keychain). iOS is not a shipping target yet.
 *
 * A codec (rather than a storage abstraction) lets [AppPreferences] keep exposing the
 * decrypted key reactively for the Settings "copy key" / masked-display flow.
 */
expect class SecretStore() {

    /** Encrypt [plaintext] into an opaque, storable token. */
    fun encrypt(plaintext: String): String

    /**
     * Decrypt a token produced by [encrypt]. Returns null when [token] is not valid
     * ciphertext — e.g. a legacy plaintext key written before encryption was added,
     * which the caller should treat as plaintext and migrate.
     */
    fun decrypt(token: String): String?
}
