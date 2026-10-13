package convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureComposeMultiplatform(
    extension: KotlinMultiplatformExtension,
) = extension.apply {
    val android = (this as ExtensionAware).extensions.getByType<KotlinMultiplatformAndroidLibraryExtension>()
    // AGP 9 turns Android resources off for KMP library modules, and Compose Multiplatform
    // resources (Res.*) ship through them: without this, Res lookups fail at runtime on Android.
    android.androidResources.enable = true
    // Res under the module's namespace; the default derives it from the Gradle group, which
    // generating a project under a new name does not rewrite.
    project.extensions.getByType<ComposeExtension>().extensions.getByType<ResourcesExtension>()
        .packageOfResClass = "${android.namespace}.generated.resources"

    val composeDeps = extensions.getByType<ComposePlugin.Dependencies>()
    sourceSets.apply {
        androidMain {
            dependencies {
                implementation(composeDeps.preview)
                implementation(libs.androidx.activity.compose.get())
            }
        }
        commonMain {
            dependencies {
                implementation(composeDeps.runtime)
                implementation(composeDeps.foundation)
                implementation(composeDeps.material3)
                implementation(composeDeps.ui)
                implementation(composeDeps.components.resources)

                implementation(project(":core:navigation"))

                implementation(libs.androidx.lifecycle.viewmodelCompose.get())
                implementation(libs.androidx.lifecycle.runtimeCompose.get())
                implementation(libs.navigation.compose.get())
            }
        }
    }
}