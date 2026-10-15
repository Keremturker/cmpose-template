package com.kturker.multiplatform.server.local

import com.kturker.multiplatform.server.TestKeys
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.ServerSocket
import java.net.URI
import java.sql.DriverManager
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocalServerTest {
    private val dir = createTempDirectory("local-server").toFile()

    @AfterTest fun cleanUp() {
        dir.deleteRecursively()
    }

    @Test fun `local server answers health on loopback and announces readiness`() {
        val port = ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { it.localPort }
        val output = StringBuilder()
        LocalServer.start(dir, LocalSettings(port, "template-test"), jwks = TestKeys.jwkProvider(), out = output::appendLine).use {
            val code = (URI("http://127.0.0.1:$port/health").toURL().openConnection() as HttpURLConnection).responseCode
            assertEquals(200, code)
            assertContains(output, "LOCAL_SERVER_READY port=$port")
        }
    }

    @Test fun `busy port stops with the holder in the message`() {
        ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { held ->
            val error = assertFailsWith<LocalSetupException> {
                LocalServer.start(dir, LocalSettings(held.localPort, "p"), jwks = TestKeys.jwkProvider(), out = {})
            }
            assertContains(error.message!!, "${held.localPort}")
        }
    }

    @Test fun `a failure after the database started releases it and is reported as a setup error`() {
        // A table Flyway does not know about makes migrate() refuse the non-empty schema.
        LocalDatabase.start(dir).use { db ->
            DriverManager.getConnection(db.jdbcUrl, db.user, db.password).use { c ->
                c.createStatement().execute("CREATE TABLE stray(v text)")
            }
        }
        val port = ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { it.localPort }
        val error = assertFailsWith<LocalSetupException> {
            LocalServer.start(dir, LocalSettings(port, "p"), jwks = TestKeys.jwkProvider(), out = {})
        }
        assertTrue(error.message!!.isNotBlank())
        // The embedded PostgreSQL was stopped and its folder lock released, so the same folder starts again.
        LocalDatabase.start(dir).use { db -> assertTrue(db.jdbcUrl.endsWith("/app")) }
        assertTrue(PortCheck.isFree(port))
    }
}
