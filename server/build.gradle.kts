plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

// Unscanned capability module (the calls pattern): reflection + raw
// framework calls live here — the tool plugin's scanner does not run on
// this module. The tool calls these classes in-process.
android {
    namespace = "com.lightphone.rcsmessages.server"
    compileSdk = rootProject.ext["compileSdk"] as Int

    defaultConfig { minSdk = rootProject.ext["minSdk"] as Int }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(rootProject.ext["jvmTarget"] as String)) }
}

dependencies {
    implementation(libs.kotlinx.coroutines)
}
