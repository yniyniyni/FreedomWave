package org.freedomwave.data.store

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

/**
 * iOS Keychain-backed secret store.
 *
 * [encrypt] stores the API key as a kSecClassGenericPassword item in the iOS
 * Keychain and returns a sentinel marker for DataStore persistence. On next
 * login the old item is deleted and replaced.
 *
 * [decrypt] retrieves the key from Keychain when given the sentinel, or returns
 * null for any other token — letting [AppPreferences] treat it as a legacy
 * plaintext value (which also triggers automatic migration via [AppPreferences.getApiKey]).
 */
@OptIn(ExperimentalForeignApi::class)
actual class SecretStore {

    actual fun encrypt(plaintext: String): String {
        deleteExisting()

        val nsValue = NSString.create(string = plaintext)
        val valueData = nsValue.dataUsingEncoding(NSUTF8StringEncoding)
            ?: error("Failed to encode API key as UTF-8 data")

        val addDict = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, forKey = kSecClass as NSString)
            setObject(kSecAttrAccessibleWhenUnlockedThisDeviceOnly, forKey = kSecAttrAccessible as NSString)
            setObject(SERVICE, forKey = kSecAttrService as NSString)
            setObject(ACCOUNT, forKey = kSecAttrAccount as NSString)
            setObject(valueData, forKey = kSecValueData as NSString)
        }

        val status = SecItemAdd(addDict as CFDictionaryRef, null)
        if (status != errSecSuccess) {
            error("SecItemAdd failed with OSStatus $status")
        }
        return SENTINEL
    }

    actual fun decrypt(token: String): String? {
        if (token != SENTINEL) return null

        val queryDict = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, forKey = kSecClass as NSString)
            setObject(SERVICE, forKey = kSecAttrService as NSString)
            setObject(ACCOUNT, forKey = kSecAttrAccount as NSString)
            setObject(kCFBooleanTrue, forKey = kSecReturnData as NSString)
            setObject(kSecMatchLimitOne, forKey = kSecMatchLimit as NSString)
        }

        return memScoped {
            val result = alloc<COpaquePointerVar>()
            val status = SecItemCopyMatching(queryDict as CFDictionaryRef, result.ptr)
            if (status != errSecSuccess) return@memScoped null
            if (result.value == null) return@memScoped null

            // result.value is a CFDataRef (COpaquePointer), NOT an ObjC object.
            // Casting to NSData always fails in Kotlin/Native. Use CFBridgingRelease
            // to toll-free bridge the CFDataRef to an NSData instance.
            val rawData = CFBridgingRelease(result.value) as? NSData
                ?: return@memScoped null

            val nsString = NSString.create(data = rawData, encoding = NSUTF8StringEncoding)
                ?: return@memScoped null

            nsString as String
        }
    }

    private fun deleteExisting() {
        val queryDict = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, forKey = kSecClass as NSString)
            setObject(SERVICE, forKey = kSecAttrService as NSString)
            setObject(ACCOUNT, forKey = kSecAttrAccount as NSString)
        }
        SecItemDelete(queryDict as CFDictionaryRef)
    }

    companion object {
        /** Keychain service identifier -- scopes the item to this app. */
        private const val SERVICE = "com.freedomwave.api"

        /** Keychain account identifier for the API key item. */
        private const val ACCOUNT = "api_key"

        /**
         * Opaque token persisted to DataStore. [decrypt] recognises this sentinel
         * and retrieves the real key from Keychain; anything else is treated as
         * a legacy plaintext value for migration.
         */
        private const val SENTINEL = "keychain:freedomwave_api_key"
    }
}
