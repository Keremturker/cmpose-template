plugins {
    alias(libs.plugins.kmp.library.plugin)
    alias(libs.plugins.compose.multiplatform.plugin)
    alias(libs.plugins.aboutLibraries)
}

kotlin {
    // The umbrella "Shared" framework consumed by the iOS app, and the only framework
    // in the project: the KMP library convention declares the iOS targets without one,
    // and every other module is linked into this one.
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            export(projects.core.ads)
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.database)
            implementation(projects.core.domain)
            implementation(projects.core.image)
            implementation(projects.core.multilang)
            implementation(projects.core.navigation)
            implementation(projects.core.about)
            implementation(projects.core.review)
            implementation(projects.core.update)
            implementation(projects.core.network)
            implementation(projects.core.room)
            implementation(projects.feature.home.data)
            implementation(projects.feature.home.contract)
            implementation(projects.feature.home.domain)
            implementation(projects.feature.home.presentation)
            implementation(projects.feature.onboarding.data)
            implementation(projects.feature.onboarding.domain)
            implementation(projects.feature.onboarding.presentation)
            implementation(projects.feature.onboarding.contract)
            implementation(projects.core.designsystem)
            api(projects.core.ads)
            implementation(projects.core.purchases)
            implementation(projects.feature.monetization.contract)
            implementation(projects.feature.monetization.presentation)
        }
        // KoinGraphTest reads constructors through Kotlin reflection, which is JVM only.
        getByName("androidHostTest").dependencies {
            implementation(libs.kotlin.reflect)
        }
    }
}

// Open source licences (core/about's licences screen): generated from this module's dependencies, which
// are the app's, on every build; the JSON is git-ignored
aboutLibraries {
    export {
        outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
        prettyPrint = false
    }
    collect {
        fetchRemoteLicense = false
        fetchRemoteFunding = false
    }
}

// Every Compose task that reads composeResources runs after the export
val composeResourcesTaskPrefixes = listOf(
    "prepareComposeResourcesTaskFor",
    "copyNonXmlValueResourcesFor",
    "convertXmlValueResourcesFor"
)
tasks.matching { task -> composeResourcesTaskPrefixes.any { task.name.startsWith(it) } }.configureEach {
    dependsOn("exportLibraryDefinitions")
}
