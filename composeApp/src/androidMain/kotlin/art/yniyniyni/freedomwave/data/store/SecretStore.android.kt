package art.yniyniyni.freedomwave.data.store

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES/GCM encryption backed by a non-exportable AndroidKeyStore key.
 * Token format: "v1:" + base64(iv) + ":" + base64(ciphertext).
 */
actual class SecretStore {

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    actual fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val ciphertext = cipher.doFinal(plaintext.encodeToByteArray())
        return "$PREFIX:${cipher.iv.b64()}:${ciphertext.b64()}"
    }

    actual fun decrypt(token: String): String? {
        val parts = token.split(":")
        if (parts.size != 3 || parts[0] != PREFIX) return null
        return runCatching {
            val iv = parts[1].b64()
            val ciphertext = parts[2].b64()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv))
            cipher.doFinal(ciphertext).decodeToString()
        }.getOrNull()
    }

    private fun ByteArray.b64(): String = Base64.encodeToString(this, Base64.NO_WRAP)
    private fun String.b64(): ByteArray = Base64.decode(this, Base64.NO_WRAP)

    private companion object {
        const val KEYSTORE       = "AndroidKeyStore"
        const val KEY_ALIAS      = "freedomwave_api_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val PREFIX         = "v1"
        const val TAG_BITS       = 128
    }
}
