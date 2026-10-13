package convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class FeatureDataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.kturker.multiplatform.kmp.library")
            }

            extensions.configure(KotlinMultiplatformExtension::class.java) {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(project(":core:domain"))
                    implementation(project(":core:network"))
                    implementation(libs.ktor.client.core.get())
                }
            }
        }
    }
}