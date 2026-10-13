package convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class FeatureContractConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.kturker.multiplatform.kmp.library")
            }

            extensions.configure(KotlinMultiplatformExtension::class.java) {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(project(":core:navigation"))
                }
            }
        }
    }
}