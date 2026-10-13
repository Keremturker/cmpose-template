plugins {
    alias(libs.plugins.feature.presentation.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(dependencyNotation = projects.feature.sample.contract)
            implementation(dependencyNotation = projects.feature.sample.domain)
        }
    }
}
