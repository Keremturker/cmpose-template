package convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

/**
 * Lets iOS test executables find the Swift back-deployment libraries
 * (`swiftCompatibility56`, `swiftCompatibilityConcurrency`, `swiftCompatibilityPacks`).
 *
 * Swift code shipped in a klib (RevenueCat's cinterop, for example) auto-links these static
 * libraries, and its klib carries a library search path to the toolchain of the Xcode it was
 * built with. When that Xcode is not installed, the Kotlin/Native linker cannot find them and the
 * test link fails with undefined `__swift_FORCE_LOAD_$_swiftCompatibility56` symbols. The app
 * itself is linked by Xcode, which adds the search path on its own, so only test binaries need it.
 *
 * The path comes from the active toolchain at build time (`xcrun --find swiftc`, which honours
 * `DEVELOPER_DIR` and `xcode-select`), never from a fixed Xcode location. A module that links no
 * Swift code is unaffected: a search path alone links nothing.
 */
internal fun Project.linkIosTestsAgainstSwiftRuntime(extension: KotlinMultiplatformExtension) {
    if (!System.getProperty("os.name").startsWith("Mac")) return

    val swiftLibDir = providers.exec {
        commandLine("xcrun", "--find", "swiftc")
        isIgnoreExitValue = true
    }.standardOutput.asText.map { swiftc ->
        // <toolchain>/usr/bin/swiftc -> <toolchain>/usr/lib/swift
        swiftc.trim().takeIf { it.isNotEmpty() }?.let { File(it).parentFile.parentFile.resolve("lib/swift") }
    }

    extension.targets.withType<KotlinNativeTarget>().configureEach {
        val platform = when (konanTarget) {
            KonanTarget.IOS_SIMULATOR_ARM64, KonanTarget.IOS_X64 -> "iphonesimulator"
            KonanTarget.IOS_ARM64 -> "iphoneos"
            else -> return@configureEach
        }
        binaries.withType<TestExecutable>().configureEach {
            swiftLibDir.orNull?.resolve(platform)?.takeIf { it.isDirectory }?.let { dir ->
                linkerOpts("-L${dir.path}")
            }
        }
    }
}
