plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.light.sdk)
}

android {
    compileSdk = rootProject.ext["compileSdk"] as Int

    signingConfigs {
        // Workspace dev signing (same key as the SDK tools/emulator). Inside
        // an SDK checkout (Light's Tool Library builder stages this tool/
        // module into the baked-in SDK repo) the keys live at ../sdk/keys.
        create("lightsdkDev") {
            storeFile = file(
                listOf("../../light-phone/light-sdk/sdk/keys/lightsdk-dev.jks", "../sdk/keys/lightsdk-dev.jks")
                    .map(::file).first { it.exists() }
            )
            storePassword = "android"
            keyAlias = "lightsdk-dev"
            keyPassword = "android"
        }
    }

    defaultConfig {
        minSdk = rootProject.ext["minSdk"] as Int
        targetSdk = rootProject.ext["targetSdk"] as Int

        // Consumed by the plugin's generated manifest (SDK_VERSION metadata).
        manifestPlaceholders["sdkVersion"] = property("sdkVersion") as String
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("lightsdkDev")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(rootProject.ext["jvmTarget"] as String))
    }
}

// Never scans: drop the SDK's bundled QR scanner stack (needs compileSdk 37;
// carries ~20 MB of native libs). Per-dependency excludes don't prune through
// the composite-build project substitution, so drop the groups wholesale.
configurations.configureEach {
    exclude(group = "com.github.markusfisch")
    exclude(group = "com.google.mlkit")
    exclude(group = "androidx.camera")
}

// Inside an SDK checkout the SDK modules are sibling projects; in this
// workspace the included ../light-phone/light-sdk build substitutes the module artifacts.
val inSdkRepo = file("../sdk").exists()

dependencies {
    implementation(
        if (inSdkRepo) project(":sdk:client")
        else "com.thelightphone:sdk-client"   // LightScreen, LightActivity
    )
    implementation(libs.kotlinx.coroutines)
}
