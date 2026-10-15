plugins {
    alias(libs.plugins.feature.presentation.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.sharing.contract)
            // LoginDestination: an invite opened while signed out waits for the sign-in
            implementation(projects.feature.account.contract)
            // Space, Member, InviteLink, InvitePreview and the paths of the spaces, members and invites calls
            implementation(projects.core.api)
            // AuthService, SessionTokens (the 401 rule), CurrentActivity for Android's share sheet
            implementation(projects.core.auth)
            // The backend's HttpClient (BACKEND_CLIENT), BaseRepository
            implementation(projects.core.network)
            // DataStore: the selected space and the invite waiting for a sign-in
            implementation(projects.core.database)
            implementation(libs.ktor.client.core)
        }
        commonTest.dependencies {
            implementation(projects.core.network)
            implementation(libs.ktor.client.mock)
        }
    }
}

// GitLive's iOS klibs (through core:auth) ask the linker for the Firebase frameworks, which only the Xcode
// app has (SPM): an iOS test executable cannot link ("framework 'FirebaseCore' not found"). This module's
// tests run on the JVM instead (testAndroidHostTest).
tasks.matching { it.name.startsWith("linkDebugTestIos") || it.name.matches(Regex("ios\\w*Test")) }.configureEach {
    enabled = false
}
