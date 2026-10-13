plugins {
    alias(libs.plugins.feature.presentation.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.room)
            implementation(projects.feature.notes.contract)
        }
    }
}
