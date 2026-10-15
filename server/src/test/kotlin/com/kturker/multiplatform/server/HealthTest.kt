package com.kturker.multiplatform.server

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthTest {
    @Test
    fun `health answers without a token`() = testApplication {
        application { module(TestKeys.deps(db = null)) }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"status":"ok"}""", response.bodyAsText())
    }

    @Test
    fun `unknown routes answer with the shared error body`() = testApplication {
        application { module(TestKeys.deps(db = null)) }
        val response = client.get("/nope")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("""{"error":"Not found","code":"not_found"}""", response.bodyAsText())
    }

    @Test
    fun `config reads the environment and rejects missing values`() {
        val env = mapOf(
            "PORT" to "8080", "DB_URL" to "jdbc:postgresql://db/app", "DB_USER" to "app",
            "DB_PASSWORD" to "x", "FIREBASE_PROJECT_ID" to "example-project", "FILES_DIR" to "/data/files",
        )
        assertEquals("example-project", ServerConfig.fromEnv(env).firebaseProjectId)
        val error = kotlin.runCatching { ServerConfig.fromEnv(env - "DB_PASSWORD") }.exceptionOrNull()
        assertEquals("Missing environment variable DB_PASSWORD", error?.message)
    }
}
