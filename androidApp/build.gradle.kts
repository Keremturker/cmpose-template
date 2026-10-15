plugins {
    alias(libs.plugins.multiplatform.application.plugin)
    // cmpose:backend
    alias(libs.plugins.googleServices) apply false
    // /cmpose:backend
}

// cmpose:backend
// Firebase app config (sign-in): the google-services plugin reads google-services.json into resources
// (default_web_client_id for Google sign-in, the Firebase app ID and API key). The file is per developer and
// git-ignored; without it the plugin is not applied, the app still builds, and sign-in reports Unavailable.
if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")

// /cmpose:backend
dependencies {
    implementation(projects.shared)
    // cmpose:backend
    // App.kt installs core:auth's CurrentActivity before Koin starts
    implementation(projects.core.auth)
    // /cmpose:backend
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
}

// Network monitor: release builds get the no-op artifact, so the real monitor
// (its manifest, call database and notifications) ships only in debug builds.
// The cmpose.dev generator deletes this whole block (first comment line through the closing brace).
configurations.matching { it.isCanBeResolved && (it.name.startsWith("release") || it.name.contains("Release")) }.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.kturker:networkmonitor"))
            .using(module("com.kturker:networkmonitor-noop:${libs.versions.networkMonitor.get()}"))
    }
}
