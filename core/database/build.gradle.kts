plugins {
    alias(libs.plugins.kmp.library.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(dependencyNotation = libs.androidx.datastore)
            api(dependencyNotation = libs.androidx.datastore.preferences)
        }
    }
}