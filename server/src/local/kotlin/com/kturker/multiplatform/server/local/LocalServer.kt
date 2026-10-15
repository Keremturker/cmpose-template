package com.kturker.multiplatform.server.local

import com.auth0.jwk.JwkProvider
import com.kturker.multiplatform.server.AppDeps
import com.kturker.multiplatform.server.ServerConfig
import com.kturker.multiplatform.server.db.connectDatabase
import com.kturker.multiplatform.server.module
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File

private const val STOP_GRACE_MS = 1_000L
private const val STOP_TIMEOUT_MS = 3_000L

/** The running local stack; closing stops Ktor first, then PostgreSQL. */
class LocalServer private constructor(
    private val engine: EmbeddedServer<*, *>,
    private val database: LocalDatabase,
    private val pidFile: File
) : AutoCloseable {
    override fun close() {
        engine.stop(STOP_GRACE_MS, STOP_TIMEOUT_MS)
        database.close()
        pidFile.delete()
    }

    companion object {
        /**
         * Starts PostgreSQL, migrates it and serves on loopback. Every failure is a [LocalSetupException];
         * whatever had started by then (Ktor, PostgreSQL) is stopped first, so no process keeps the data folder.
         */
        fun start(
            dir: File,
            settings: LocalSettings,
            jwks: JwkProvider,
            out: (String) -> Unit
        ): LocalServer {
            if (!PortCheck.isFree(settings.port)) {
                throw LocalSetupException("Port ${settings.port} is in use:\n${PortCheck.holder(settings.port)}")
            }
            val database = startDatabase(dir)
            return try {
                serve(dir, settings, jwks, out, database)
            } catch (e: Exception) {
                database.close()
                throw setupFailure("The local server did not start", e)
            }
        }

        private fun startDatabase(dir: File): LocalDatabase = try {
            LocalDatabase.start(dir)
        } catch (e: Exception) {
            throw setupFailure("Embedded PostgreSQL did not start in ${dir.resolve("postgres").path}", e)
        }

        private fun serve(
            dir: File,
            settings: LocalSettings,
            jwks: JwkProvider,
            out: (String) -> Unit,
            database: LocalDatabase
        ): LocalServer {
            val files = dir.resolve("files").apply { mkdirs() }
            val config = ServerConfig(
                port = settings.port,
                dbUrl = database.jdbcUrl,
                dbUser = database.user,
                dbPassword = database.password,
                firebaseProjectId = settings.firebaseProjectId,
                filesDir = files.absolutePath
            )
            // Migrated before Ktor starts, so a Flyway failure never leaves a half-started engine behind.
            val db = connectDatabase(config)
            val engine = embeddedServer(Netty, port = settings.port, host = "127.0.0.1") {
                module(AppDeps(config, jwks, db))
            }
            try {
                engine.start(wait = false)
                val pidFile = dir.resolve("server.pid").apply { writeText(ProcessHandle.current().pid().toString()) }
                out("LOCAL_SERVER_READY port=${settings.port}")
                return LocalServer(engine, database, pidFile)
            } catch (e: Exception) {
                engine.stop(STOP_GRACE_MS, STOP_TIMEOUT_MS)
                throw e
            }
        }

        private fun setupFailure(
            what: String,
            cause: Exception
        ) = cause as? LocalSetupException
            ?: LocalSetupException("$what: ${cause.message ?: cause::class.simpleName}", cause)
    }
}
