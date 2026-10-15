package com.kturker.multiplatform.server

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.kturker.multiplatform.server.auth.FIREBASE_AUTH
import com.kturker.multiplatform.server.auth.principalOrThrow
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.security.KeyPairGenerator
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthTest {
    private fun ApplicationTestBuilder.app() {
        application {
            module(TestKeys.deps(db = null))
            routing { authenticate(FIREBASE_AUTH) { get("/whoami") { call.respondText(call.principalOrThrow().uid) } } }
        }
    }

    @Test
    fun `a valid firebase token yields the uid`() = testApplication {
        app()
        val response = client.get("/whoami") { bearerAuth(TestKeys.token(uid = "abc")) }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("abc", response.bodyAsText())
    }

    @Test
    fun `no token is 401 with the shared body`() = testApplication {
        app()
        val response = client.get("/whoami")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("""{"error":"Unauthorized","code":"unauthorized"}""", response.bodyAsText())
    }

    @Test
    fun `a token for another firebase project is rejected`() = testApplication {
        app()
        val other = TestKeys.token(audience = "someone-else", issuer = "https://securetoken.google.com/someone-else")
        assertEquals(HttpStatusCode.Unauthorized, client.get("/whoami") { bearerAuth(other) }.status)
    }

    @Test
    fun `a token with the right audience but a foreign issuer is rejected`() = testApplication {
        app()
        val forged = TestKeys.token(issuer = "https://evil.example/${TestKeys.PROJECT_ID}")
        assertEquals(HttpStatusCode.Unauthorized, client.get("/whoami") { bearerAuth(forged) }.status)
    }

    @Test
    fun `an expired token is rejected`() = testApplication {
        app()
        val expired = TestKeys.token(expiresInSec = -120)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/whoami") { bearerAuth(expired) }.status)
    }

    @Test
    fun `a token signed by an unknown key is rejected`() = testApplication {
        app()
        val stranger = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val forged = TestKeys.token(signWith = stranger.private)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/whoami") { bearerAuth(forged) }.status)
    }

    @Test
    fun `a token whose kid is not published is rejected`() = testApplication {
        app()
        assertEquals(HttpStatusCode.Unauthorized, client.get("/whoami") { bearerAuth(TestKeys.token(kid = "gone")) }.status)
    }

    @Test
    fun `a token without a subject is rejected`() = testApplication {
        app()
        assertEquals(HttpStatusCode.Unauthorized, client.get("/whoami") { bearerAuth(TestKeys.token(uid = "")) }.status)
    }

    @Test
    fun `the production key provider fetches a kid it has not cached yet`() {
        var fetches = 0
        val upstream = object : JwkProvider {
            override fun get(keyId: String): Jwk {
                fetches++
                return TestKeys.jwkProvider.get(TestKeys.KID)
            }
        }
        val cached = GuavaCachedJwkProvider(upstream, 10, 6, TimeUnit.HOURS)
        cached.get(TestKeys.KID)
        cached.get(TestKeys.KID)
        assertEquals(1, fetches)
        runCatching { cached.get("new-kid") }
        assertEquals(2, fetches)
    }
}
