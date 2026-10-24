pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // sdk:ui api-exposes com.github.lightphone:light-keyboard (font);
        // the composite resolves it against the consumer's repositories.
        maven {
            name = "JitPack"
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "rcs-messages"

include(":tool")
include(":server")

// Single-module tool (the tasks/passes pattern): `:tool` is the real LightOS
// tool (lighttool.toml + light-sdk tool plugin, LightScreen UI). The RCS
// client layer will live in the same module until it earns a split.
// Consumes the SDK as an included build (workspace: light-sdk lives inside
// the sibling light-phone workspace checkout).
includeBuild("../light-phone/light-sdk") {
    dependencySubstitution {
        substitute(module("com.thelightphone:sdk-ui")).using(project(":sdk:ui"))
        substitute(module("com.thelightphone:sdk-client")).using(project(":sdk:client"))
        substitute(module("com.thelightphone:sdk-server")).using(project(":sdk:server"))
        substitute(module("com.thelightphone:sdk-shared")).using(project(":sdk:shared"))
    }
}
