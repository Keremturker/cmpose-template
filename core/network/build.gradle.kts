plugins {
    alias(libs.plugins.kmp.library.plugin)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.domain)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)
                // Network images for Coil (core:image): Coil finds the Ktor fetcher on the classpath
                implementation(libs.coil.network.ktor3)
                // Network monitor: release APKs swap in networkmonitor-noop (androidApp); iOS gates it at runtime
                implementation(libs.networkMonitor.debug)
            }
        }

        commonTest.dependencies {
            implementation(libs.kermit.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.android)

        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}