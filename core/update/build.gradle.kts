plugins {
    alias(libs.plugins.kmp.library.plugin)
    alias(libs.plugins.compose.multiplatform.plugin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.about)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.play.app.update)
            implementation(libs.play.app.update.ktx)
        }
    }
}
