package com.kturker.multiplatform.server.db

import com.kturker.multiplatform.server.ServerConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

private const val MAX_POOL_SIZE = 4

fun connectDatabase(config: ServerConfig): Database {
    val source = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = config.dbUrl
            username = config.dbUser
            password = config.dbPassword
            maximumPoolSize = MAX_POOL_SIZE
        }
    )
    try {
        Flyway.configure().dataSource(source).load().migrate()
    } catch (e: org.flywaydb.core.api.FlywayException) {
        source.close() // a failed migration must not leave the pool's connections open
        throw e
    }
    return Database.connect(source)
}
