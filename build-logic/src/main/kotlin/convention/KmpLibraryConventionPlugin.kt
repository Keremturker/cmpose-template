package convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.kturker.multiplatform.detekt")
                apply(plugin = libs.plugins.kotlinMultiplatform.get().pluginId)
                apply(plugin = libs.plugins.androidKotlinMultiplatformLibrary.get().pluginId)
                apply(plugin = libs.plugins.kotlin.serialization.get().pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                configureKotlinMultiplatform(this)
                configureKmpWithKsp(this)
                linkIosTestsAgainstSwiftRuntime(this)
            }
        }
    }
}
