plugins {
    alias(libs.plugins.kmp.library.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.revenuecat.core)
            implementation(projects.core.domain)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
