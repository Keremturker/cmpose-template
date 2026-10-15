package com.kturker.multiplatform.server.local

import com.kturker.multiplatform.server.googleJwkProvider
import java.io.File
import kotlin.system.exitProcess

/** `./gradlew :server:runLocal` — args[0] is the .local-server folder. */
fun main(args: Array<String>) {
    val dir = File(args.firstOrNull() ?: ".local-server").apply { mkdirs() }
    val server = try {
        LocalServer.start(dir, LocalSettings.read(dir), googleJwkProvider(), ::println)
    } catch (e: LocalSetupException) {
        System.err.println(e.message)
        exitProcess(1)
    }
    Runtime.getRuntime().addShutdownHook(Thread { server.close() })
    Thread.currentThread().join()
}
