package com.kturker.multiplatform.server

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.SigningKeyNotFoundException
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import java.util.Date

/** A local RSA key that stands in for Google's securetoken keys in tests. */
object TestKeys {
    const val PROJECT_ID = "template-test"
    const val KID = "test-key-1"
    val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

    val jwkProvider = object : JwkProvider {
        override fun get(keyId: String): Jwk {
            if (keyId != KID) throw SigningKeyNotFoundException("unknown kid $keyId", null)
            val key = keyPair.public as RSAPublicKey
            val b64 = Base64.getUrlEncoder().withoutPadding()
            return Jwk.fromValues(
                mapOf(
                    "kid" to KID, "kty" to "RSA", "alg" to "RS256", "use" to "sig",
                    "n" to b64.encodeToString(key.modulus.toByteArray().dropWhile { it == 0.toByte() }.toByteArray()),
                    "e" to b64.encodeToString(key.publicExponent.toByteArray()),
                ),
            )
        }
    }

    /** The same provider as a call, for callers outside this package (the local runner tests). */
    fun jwkProvider(): JwkProvider = jwkProvider

    fun config(filesDir: String = "build/test-files") = ServerConfig(
        port = 0, dbUrl = "", dbUser = "", dbPassword = "", firebaseProjectId = PROJECT_ID, filesDir = filesDir,
    )

    fun deps(db: org.jetbrains.exposed.sql.Database?, filesDir: String = "build/test-files") =
        AppDeps(config(filesDir), jwkProvider, db)

    fun dbDeps(filesDir: String = kotlin.io.path.createTempDirectory("files").toString()) =
        AppDeps(config(filesDir), jwkProvider, TestDb.database)

    fun token(
        uid: String = "u1",
        email: String? = "$uid@example.com",
        name: String? = null,
        audience: String = PROJECT_ID,
        issuer: String = "https://securetoken.google.com/$PROJECT_ID",
        expiresInSec: Long = 3600,
        kid: String = KID,
        signWith: PrivateKey = keyPair.private,
    ): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withKeyId(kid)
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(uid)
            .withIssuedAt(Date(now - 1_000))
            .withExpiresAt(Date(now + expiresInSec * 1_000))
            .apply { if (email != null) withClaim("email", email) }
            .apply { if (name != null) withClaim("name", name) }
            .sign(Algorithm.RSA256(null, signWith as RSAPrivateKey))
    }
}
