plugins {
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "art.yniyniyni.freedomwave.shared"
        compileSdk = 36
        minSdk = 31
        withHostTest {}
    }

    // iOS targets: compile-verified stubs, no Xcode project yet
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            @Suppress("DEPRECATION") implementation(compose.material3)
            @Suppress("DEPRECATION") implementation(compose.materialIconsExtended)
            @Suppress("DEPRECATION") implementation(compose.components.resources)
            @Suppress("DEPRECATION") implementation(compose.components.uiToolingPreview)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)

            // Networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)

            // Serialization / Coroutines / Datetime
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Local storage
            implementation(libs.datastore.preferences.core)

            // Image loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // QR Code
            implementation(libs.qrose)

            // Logging
            implementation(libs.kermit)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.datastore.preferences)
            implementation(libs.koin.androidx.compose)
            implementation("androidx.biometric:biometric:1.1.0")
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
        }
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
}
