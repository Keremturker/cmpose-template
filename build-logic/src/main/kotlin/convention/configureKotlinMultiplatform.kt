package convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import config.AppConfig
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform(
    extension: KotlinMultiplatformExtension
) = extension.apply {
    val moduleName = path.split(":").drop(2).joinToString(".")
    // Top-level KMP modules (e.g. :shared) yield an empty moduleName; fall back to
    // the project name so their namespace stays distinct from the application module.
    val moduleNamespace =
        if (moduleName.isNotEmpty()) "${AppConfig.applicationId}.$moduleName" else "${AppConfig.applicationId}.${project.name}"

    // AGP 9: the Android target of a KMP library is configured through the
    // com.android.kotlin.multiplatform.library plugin's `android {}` DSL,
    // which replaces the old `androidTarget()` + top-level `android {}` block.
    (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
        namespace = moduleNamespace
        compileSdk = AppConfig.compileSdkVersion
        minSdk = AppConfig.minSdkVersion
        // commonTest also runs on the JVM (testAndroidHostTest), next to the iOS simulator
        // (iosSimulatorArm64Test); `./gradlew allTests` runs both.
        withHostTest {}
    }

    // iosX64 (Intel simulator) was dropped in Compose Multiplatform 1.11; modern
    // Apple-Silicon Macs use iosSimulatorArm64. No framework here: only :shared
    // declares one (the framework the iOS app links), and every other module is
    // linked into it. A framework per module would only add unused link tasks.
    iosArm64()
    iosSimulatorArm64()

    sourceSets.apply {
        commonMain {
            dependencies {
                implementation(libs.koin.core.get())
                implementation(libs.koin.annotations.get())
                implementation(libs.kotlinx.serialization.json.get())
                if (project.path != LOGGING_MODULE) {
                    implementation(project(LOGGING_MODULE))
                }
            }
        }
        androidMain {
            dependencies {
                implementation(libs.koin.android.get())
                implementation(libs.koin.androidx.compose.get())
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test.get())
                implementation(libs.kotlinx.coroutines.test.get())
                implementation(libs.turbine.get())
            }
        }
    }

    // Print why a test failed (its assertion message), not only which test failed.
    project.tasks.withType(AbstractTestTask::class.java).configureEach {
        testLogging {
            events(TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

// Every KMP module logs through Kermit (core:logging); core:logging itself is the one exception
private const val LOGGING_MODULE = ":core:logging"
