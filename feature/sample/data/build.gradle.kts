plugins {
    alias(libs.plugins.feature.data.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(dependencyNotation = projects.feature.sample.domain)
        }
    }
}
