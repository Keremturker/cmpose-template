plugins {
    alias(libs.plugins.feature.presentation.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.database)
            implementation(projects.core.about)
            implementation(projects.feature.home.contract)
            implementation(projects.feature.home.domain)
            implementation(projects.feature.monetization.contract)
            implementation(projects.feature.onboarding.contract)
            implementation(projects.feature.onboarding.domain)
        }
    }
}