plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.lightphone.rcsprobe"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.lightphone.rcsprobe"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    // Emulator-only: platform (AOSP testkey) signing for INTERNAL-protection
    // RCS permission experiments. The keystore is local (gitignored); public
    // builds fall back to debug signing.
    val platformKeystore = file("keys/platform.jks")
    if (platformKeystore.exists()) {
        signingConfigs {
            create("platform") {
                storeFile = platformKeystore
                storePassword = "android"
                keyAlias = "platform"
                keyPassword = "android"
            }
        }
        buildTypes.all { signingConfig = signingConfigs.getByName("platform") }
    }
    kotlin {
        compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17 }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {}
