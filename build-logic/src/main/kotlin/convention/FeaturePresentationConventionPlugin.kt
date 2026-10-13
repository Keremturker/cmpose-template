package convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class FeaturePresentationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.kturker.multiplatform.kmp.library")
                apply("com.kturker.multiplatform.compose.multiplatform")
            }

            extensions.configure(KotlinMultiplatformExtension::class.java) {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(project(":core:designsystem"))
                    implementation(project(":core:domain"))
                    implementation(project(":core:multilang"))
                    implementation(project(":core:presentation"))
                    // Every screen can ask for a review: ReviewPrompter.recordPositiveEvent()
                    implementation(project(":core:review"))
                    // Every screen can show images: Coil's AsyncImage through the app's loader
                    implementation(project(":core:image"))
                }
            }
        }
    }
}