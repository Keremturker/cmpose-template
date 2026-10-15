plugins {
    alias(libs.plugins.kmp.library.plugin)
    alias(libs.plugins.compose.multiplatform.plugin)
}

kotlin {
    // cmpose:backend
    android {
        // ImageNormalizationDeviceTest runs the real Android decoder: connectedAndroidDeviceTest on an
        // emulator or a device. In the "test" source set tree, so commonTest runs there too; a manual
        // androidDeviceTest.dependsOn(commonTest) would switch the default hierarchy off.
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    // /cmpose:backend
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            // Every module that depends on core:image can call Coil's AsyncImage
            api(libs.coil.compose)
        }
        commonTest.dependencies {
            implementation(libs.kermit.test)
        }
        // cmpose:backend
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.ext.junit)
        }
        // /cmpose:backend
    }
}
