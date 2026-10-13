plugins {
    alias(libs.plugins.feature.data.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(dependencyNotation = projects.core.room)
            implementation(dependencyNotation = projects.feature.home.domain)
        }
    }
}