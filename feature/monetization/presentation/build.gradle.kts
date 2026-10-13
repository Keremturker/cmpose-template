plugins {
    alias(libs.plugins.feature.presentation.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.about)
            implementation(projects.core.ads)
            implementation(projects.core.purchases)
            implementation(projects.feature.monetization.contract)
        }
    }
}
