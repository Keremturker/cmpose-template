plugins {
    alias(libs.plugins.kmp.library.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: every module that depends on core:logging writes through co.touchlab.kermit.Logger
            api(libs.kermit)
        }
        commonTest.dependencies {
            implementation(libs.kermit.test)
        }
    }
}
