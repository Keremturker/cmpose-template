plugins {
    alias(libs.plugins.kmp.library.plugin)
    alias(libs.plugins.compose.multiplatform.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.multilang)
            implementation(projects.core.image)
        }
    }
}