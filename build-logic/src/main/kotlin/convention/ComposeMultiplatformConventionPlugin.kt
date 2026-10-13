package convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(plugin = libs.plugins.composeMultiplatform.get().pluginId)
                apply(plugin = libs.plugins.composeCompiler.get().pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                configureComposeMultiplatform(this)
            }

            extensions.configure(KotlinMultiplatformExtension::class.java) {
                sourceSets.getByName("androidMain").dependencies {
                    implementation(libs.koin.android.get())
                    implementation(libs.koin.androidx.compose.get())
                }

                sourceSets.getByName("commonMain").dependencies {
                    implementation(libs.koin.core.get())
                    implementation(libs.koin.compose.get())
                    implementation(libs.koin.composeVM.get())
                }
            }
        }
    }
}