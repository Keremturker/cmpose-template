plugins {
    alias(libs.plugins.feature.data.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.onboarding.domain)
        }
    }
}