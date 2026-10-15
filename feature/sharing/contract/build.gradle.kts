plugins {
    alias(libs.plugins.feature.contract.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SelectedSpace hands out core:api's Space through a StateFlow: both are part of this contract
            api(projects.core.api)
            api(libs.kotlinx.coroutines.core)
        }
    }
}
