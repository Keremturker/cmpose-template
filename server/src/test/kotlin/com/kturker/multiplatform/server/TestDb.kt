package com.kturker.multiplatform.server

import com.kturker.multiplatform.server.db.connectDatabase
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

/** One embedded PostgreSQL 16 for the whole test run (real server binaries; no Docker required). */
object TestDb {
    private val postgres = EmbeddedPostgres.builder().start()

    val database: Database by lazy {
        connectDatabase(
            TestKeys.config().copy(
                dbUrl = postgres.getJdbcUrl("postgres", "postgres"),
                dbUser = "postgres",
                dbPassword = "postgres",
            ),
        )
    }

    /**
     * A database on the same server whose `users` table refuses every row. The CHECK violation's detail quotes the
     * whole row ("Failing row contains (...)"), so tests can force a DB error that carries the caller's identifiers.
     */
    val rejectingUsers: Database by lazy {
        postgres.postgresDatabase.connection.use { it.createStatement().execute("CREATE DATABASE rejecting") }
        val source = postgres.getDatabase("postgres", "rejecting")
        source.connection.use {
            it.createStatement().execute(
                "CREATE TABLE users (uid TEXT PRIMARY KEY, email TEXT, display_name TEXT, created_at BIGINT NOT NULL, " +
                    "CHECK (false))",
            )
        }
        Database.connect(source)
    }

    /** Empties every table; a block body keeps the return type `Unit` so JUnit accepts it in `@BeforeTest`. */
    fun reset() {
        transaction(database) { exec("TRUNCATE files, invites, space_members, spaces, users CASCADE") }
    }
}
