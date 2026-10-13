package convention

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.plugins.detekt.get().pluginId)
        }

        dependencies.add("detektPlugins", libs.detekt.formatting.get())

        extensions.configure(DetektExtension::class.java) {
            config.setFrom(files("$rootDir/detekt/detekt.yml"))
            buildUponDefaultConfig.set(true)
            parallel.set(true)

            source.setFrom(
                files(
                    "src/commonMain/kotlin",
                    "src/androidMain/kotlin",
                    "src/jvmMain/kotlin",
                    "src/iosMain/kotlin",
                    "src/main/kotlin"
                )
            )
        }

        tasks.withType(Detekt::class.java).configureEach {
            jvmTarget.set("21")
            reports {
                html.required.set(true)
                checkstyle.required.set(false)
                sarif.required.set(false)
                markdown.required.set(false)
            }
        }
    }
}
