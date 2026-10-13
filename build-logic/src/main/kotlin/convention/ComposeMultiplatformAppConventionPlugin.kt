package convention

import com.android.build.api.dsl.ApplicationExtension
import config.AppConfig
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import java.util.Properties

/**
 * Convention for the Android application entry-point module.
 *
 * AGP 9 no longer allows combining `com.android.application` with the Kotlin
 * Multiplatform plugin in the same module, so this is a pure Android application:
 * it consumes the `:shared` KMP library and hosts the Android entry points
 * (Application class, launcher Activity, manifest, launcher resources).
 *
 * Kotlin is compiled by AGP's built-in Kotlin support, so the
 * `org.jetbrains.kotlin.android` plugin must NOT be applied here.
 */
class ComposeMultiplatformAppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.kturker.multiplatform.detekt")
                apply(plugin = libs.plugins.androidApplication.get().pluginId)
                apply(plugin = libs.plugins.composeCompiler.get().pluginId)
            }

            extensions.configure<ApplicationExtension> {
                namespace = AppConfig.applicationId
                compileSdk = AppConfig.compileSdkVersion

                defaultConfig {
                    applicationId = AppConfig.applicationId
                    minSdk = AppConfig.minSdkVersion
                    targetSdk = AppConfig.targetSdkVersion
                    versionCode = AppConfig.versionCode
                    versionName = AppConfig.versionName
                }

                buildFeatures {
                    compose = true
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }

                // Release signing: copy keystore.properties.example to keystore.properties (git-ignored).
                // Without it the release APK stays unsigned.
                val keystoreFile = rootProject.file("keystore.properties")
                if (keystoreFile.exists()) {
                    val keystore = Properties().apply { keystoreFile.inputStream().use(::load) }
                    signingConfigs.create("release") {
                        storeFile = rootProject.file(keystore.required("storeFile"))
                        storePassword = keystore.required("storePassword")
                        keyAlias = keystore.required("keyAlias")
                        keyPassword = keystore.required("keyPassword")
                    }
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                        signingConfigs.findByName("release")?.let { signingConfig = it }
                    }
                }
            }
        }
    }
}

private fun Properties.required(key: String): String =
    requireNotNull(getProperty(key)?.takeIf { it.isNotBlank() }) {
        "keystore.properties is missing \"$key\" (see keystore.properties.example)"
    }
