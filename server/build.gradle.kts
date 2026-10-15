plugins {
    alias(libs.plugins.jetbrainsKotlinJvm)
    alias(libs.plugins.shadow)
    // build-logic on the classpath (AppConfig, the serialization plugin) without applying it: the detekt plugin
    // below is the only other build-logic plugin here, and a project without detekt drops it
    id("com.kturker.multiplatform.kmp.library") apply false
    id("com.kturker.multiplatform.detekt")
    application
}

// On the classpath through build-logic; a plugins {} request would need its marker artifact.
apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

kotlin { jvmToolchain(21) }

application { mainClass.set("com.kturker.multiplatform.server.MainKt") }

dependencies {
    implementation(projects.core.api)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.jwks.rsa)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    implementation(libs.postgresql)
    implementation(libs.hikari)
    implementation(libs.logback)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.embedded.postgres)
    testImplementation(platform(libs.embedded.postgres.bom))
}

tasks.test { useJUnitPlatform() }

// The KMP modules' allTests is the template's one test command: `./gradlew allTests` runs the server's tests too
tasks.register("allTests") {
    group = "verification"
    description = "Runs the server's tests (the JVM module's part of the root allTests)"
    dependsOn(tasks.test)
}

tasks.shadowJar {
    archiveFileName.set("server.jar")
    // Shadow 9 drops duplicates before transformers run; keep them so service files really merge.
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles() // Flyway's database plugins are found through META-INF/services
}

// Local development server: an embedded PostgreSQL with a persistent data folder and the same module().
// Kept out of `main` so the release jar (shadowJar) carries neither the runner nor the PostgreSQL binaries.
val local: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}
configurations[local.implementationConfigurationName].extendsFrom(configurations.implementation.get())
configurations[local.runtimeOnlyConfigurationName].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    "localImplementation"(libs.embedded.postgres)
    "localImplementation"(platform(libs.embedded.postgres.bom))
}

// The convention plugin only lists src/main; the local runner gets the same rules.
detekt { source.from("src/local/kotlin") }

sourceSets.test {
    compileClasspath += local.output
    runtimeClasspath += local.output
}

val localServerDir = rootProject.layout.projectDirectory.dir(".local-server")

tasks.register<JavaExec>("runLocal") {
    group = "application"
    description = "Runs the server on 127.0.0.1 with an embedded PostgreSQL kept in .local-server/"
    classpath = local.runtimeClasspath
    mainClass.set("com.kturker.multiplatform.server.local.LocalMainKt")
    args(localServerDir.asFile.absolutePath)
}

tasks.register<Delete>("resetLocal") {
    group = "application"
    description = "Deletes .local-server/ (database, files and config)"
    delete(localServerDir)
}
