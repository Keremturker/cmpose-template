plugins {
    alias(libs.plugins.kmp.library.plugin)
    alias(libs.plugins.compose.multiplatform.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kermit.test)
        }
        androidMain.dependencies {
            implementation(libs.adsMobileSdk)
            implementation(libs.ump)
            // The ads SDK pulls in an old WorkManager (and its Room) whose keep rules let R8 strip the
            // generated WorkDatabase constructor, so release builds crash at startup. Depending on a
            // current WorkManager directly replaces it with one that ships working rules.
            implementation(libs.androidx.work.runtime)
            implementation(libs.androidx.activity.compose)
        }
    }
}
