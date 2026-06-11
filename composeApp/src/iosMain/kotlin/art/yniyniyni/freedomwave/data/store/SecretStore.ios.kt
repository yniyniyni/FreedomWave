package art.yniyniyni.freedomwave.data.store

/**
 * iOS stub: stores the value as-is.
 *
 * TODO(iOS): back this with Keychain (kSecClassGenericPassword) before iOS ships.
 * Returning null from [decrypt] makes [AppPreferences] treat the stored value as
 * plaintext, so the login flow keeps working on the iOS framework build today.
 */
actual class SecretStore {
    actual fun encrypt(plaintext: String): String = plaintext
    actual fun decrypt(token: String): String? = null
}
