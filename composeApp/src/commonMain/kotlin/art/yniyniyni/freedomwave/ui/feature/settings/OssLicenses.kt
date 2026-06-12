package art.yniyniyni.freedomwave.ui.feature.settings

/** A license whose name we surface; the full text lives at the library's [OssLibrary.url]. */
enum class OssLicense(val displayName: String) {
    APACHE_2_0("Apache License 2.0"),
    MIT("MIT License"),
}

/** A bundled third-party open-source library, for the attribution screen. */
data class OssLibrary(
    val name: String,
    val copyright: String,
    val license: OssLicense,
    val url: String,
)

/**
 * Third-party OSS that actually ships in the app (test-only deps such as JUnit/Espresso/
 * ktor-client-mock are excluded — they are not distributed). Keep in sync with the version
 * catalog in gradle/libs.versions.toml.
 */
val ossLibraries: List<OssLibrary> = listOf(
    OssLibrary("Compose Multiplatform", "JetBrains s.r.o. and contributors", OssLicense.APACHE_2_0, "https://github.com/JetBrains/compose-multiplatform"),
    OssLibrary("Kotlin Standard Library", "JetBrains s.r.o. and Kotlin contributors", OssLicense.APACHE_2_0, "https://github.com/JetBrains/kotlin"),
    OssLibrary("kotlinx.coroutines", "JetBrains s.r.o.", OssLicense.APACHE_2_0, "https://github.com/Kotlin/kotlinx.coroutines"),
    OssLibrary("kotlinx.serialization", "JetBrains s.r.o.", OssLicense.APACHE_2_0, "https://github.com/Kotlin/kotlinx.serialization"),
    OssLibrary("kotlinx-datetime", "JetBrains s.r.o.", OssLicense.APACHE_2_0, "https://github.com/Kotlin/kotlinx-datetime"),
    OssLibrary("Ktor", "JetBrains s.r.o.", OssLicense.APACHE_2_0, "https://github.com/ktorio/ktor"),
    OssLibrary("AndroidX Lifecycle (multiplatform)", "JetBrains s.r.o. and The Android Open Source Project", OssLicense.APACHE_2_0, "https://github.com/JetBrains/compose-multiplatform-core"),
    OssLibrary("AndroidX Activity", "The Android Open Source Project", OssLicense.APACHE_2_0, "https://developer.android.com/jetpack/androidx"),
    OssLibrary("AndroidX Core", "The Android Open Source Project", OssLicense.APACHE_2_0, "https://developer.android.com/jetpack/androidx"),
    OssLibrary("AndroidX DataStore", "The Android Open Source Project", OssLicense.APACHE_2_0, "https://developer.android.com/jetpack/androidx"),
    OssLibrary("AndroidX Biometric", "The Android Open Source Project", OssLicense.APACHE_2_0, "https://developer.android.com/jetpack/androidx"),
    OssLibrary("AndroidX AppCompat", "The Android Open Source Project", OssLicense.APACHE_2_0, "https://developer.android.com/jetpack/androidx"),
    OssLibrary("Koin", "Kotzilla and Koin contributors", OssLicense.APACHE_2_0, "https://github.com/InsertKoinIO/koin"),
    OssLibrary("Coil", "Coil Contributors", OssLicense.APACHE_2_0, "https://github.com/coil-kt/coil"),
    OssLibrary("Kermit", "Touchlab", OssLicense.APACHE_2_0, "https://github.com/touchlab/Kermit"),
    OssLibrary("qrose", "Alexander Zhirkevich", OssLicense.MIT, "https://github.com/alexzhirkevich/qrose"),
)
