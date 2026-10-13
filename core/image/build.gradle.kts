plugins {
    alias(libs.plugins.kmp.library.plugin)
    alias(libs.plugins.compose.multiplatform.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            // Every module that depends on core:image can call Coil's AsyncImage
            api(libs.coil.compose)
        }
        commonTest.dependencies {
            implementation(libs.kermit.test)
        }
    }
}
