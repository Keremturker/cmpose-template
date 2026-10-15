import config.AppConfig

// Not the kmp.library convention plugin: it adds Koin and :core:logging (Android/iOS only),
// while these models must also build for the JVM so the server can share them.
plugins {
    // build-logic on the classpath (AppConfig, the serialization plugin) without applying it: the detekt plugin
    // below is the only other build-logic plugin here, and a project without detekt drops it
    id("com.kturker.multiplatform.kmp.library") apply false
    id("com.kturker.multiplatform.detekt")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

// On the classpath through build-logic; a plugins {} request would need its marker artifact.
apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

kotlin {
    // The server module consumes these models too.
    jvm()

    android {
        namespace = "${AppConfig.applicationId}.core.api"
        compileSdk = AppConfig.compileSdkVersion
        minSdk = AppConfig.minSdkVersion
        withHostTest {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
