plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose.compiler)
}

android {
    namespace = "art.yniyniyni.freedomwave"
    compileSdk = 36

    defaultConfig {
        applicationId = "art.yniyniyni.freedomwave"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// See buildSrc CopyComposeResources: works around the compose plugin not packaging
// converted value resources (.cvr) into a consuming app under the AGP KMP library plugin.
androidComponents {
    onVariants { variant ->
        val capName = variant.name.replaceFirstChar { it.uppercaseChar() }
        val copyTask = tasks.register<CopyComposeResources>("copy${capName}ComposeResources") {
            dependsOn(":composeApp:prepareComposeResourcesTaskForCommonMain")
            preparedResources.set(
                project(":composeApp").layout.buildDirectory.dir(
                    "generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"
                )
            )
            resourcePackage.set("freedomwave.composeapp.generated.resources")
            outputDir.set(layout.buildDirectory.dir("generated/composeResources/${variant.name}"))
        }
        variant.sources.assets?.addGeneratedSourceDirectory(copyTask) { it.outputDir }
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.glance.appwidget)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.androidx.compose)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.junit.ext)
    androidTestImplementation(libs.espresso.core)
}
