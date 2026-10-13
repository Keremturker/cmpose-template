plugins {
    alias(libs.plugins.kmp.library.plugin)
    alias(libs.plugins.compose.multiplatform.plugin)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.domain)
            }
        }
    }
}