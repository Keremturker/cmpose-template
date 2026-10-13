plugins {
    alias(libs.plugins.feature.presentation.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(dependencyNotation = projects.feature.home.contract)
            implementation(dependencyNotation = projects.feature.home.domain)
        }
    }
}