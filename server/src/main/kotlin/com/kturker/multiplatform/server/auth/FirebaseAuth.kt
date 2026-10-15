package com.kturker.multiplatform.server.auth

import com.auth0.jwk.JwkException
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.NetworkException
import com.auth0.jwk.SigningKeyNotFoundException
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.IncorrectClaimException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.MissingClaimException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.kturker.multiplatform.core.api.ErrorCode
import com.kturker.multiplatform.server.ApiException
import com.kturker.multiplatform.server.AppDeps
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import org.slf4j.LoggerFactory
import java.security.interfaces.RSAPublicKey

const val FIREBASE_AUTH = "firebase"

private const val CLOCK_LEEWAY_SEC = 30L

/** Logs why a token was refused (reason only: never the token, its uid or its email). */
internal val authLog = LoggerFactory.getLogger("com.kturker.multiplatform.server.auth.FirebaseAuth")

/** The caller as the token describes them: `sub` (uid), and the optional `email` and `name` claims. */
data class FirebasePrincipal(val uid: String, val email: String?, val name: String?)

/**
 * Verifies Firebase ID tokens with Google's published securetoken keys: RS256 signature, `iss` and `aud` bound to this
 * app's Firebase project, `exp`/`iat` with 30 s leeway, non-empty `sub` (the uid). No Admin SDK, no service account.
 * Every refusal is logged at WARN as "Token rejected: <reason>", so a 401 on the device can be explained here.
 */
fun Application.installFirebaseAuth(deps: AppDeps) {
    val projectId = deps.config.firebaseProjectId
    val verifierFor: (HttpAuthHeader) -> JWTVerifier? = { header ->
        bearerBlob(header)?.let { token -> firebaseVerifier(deps.jwkProvider, projectId, token) }
    }
    install(Authentication) {
        jwt(FIREBASE_AUTH) {
            verifier(verifierFor)
            validate { credential ->
                val uid = credential.payload.subject?.takeIf { it.isNotBlank() }
                if (uid == null) rejectToken("no subject")
                uid?.let {
                    FirebasePrincipal(
                        uid = it,
                        email = credential.payload.getClaim("email").asString(),
                        name = credential.payload.getClaim("name").asString()
                    )
                }
            }
        }
    }
}

private fun bearerBlob(header: HttpAuthHeader): String? =
    (header as? HttpAuthHeader.Single)?.takeIf { it.authScheme.equals("Bearer", ignoreCase = true) }?.blob

private fun rejectToken(reason: String) = authLog.warn("Token rejected: {}", reason)

/**
 * The verifier for one token: its `kid` picks the Google key, then signature and claims are checked. Null (a 401)
 * when the token cannot be read or its key cannot be found; each such case is logged with its reason.
 */
private fun firebaseVerifier(
    jwkProvider: JwkProvider,
    projectId: String,
    token: String
): JWTVerifier? {
    val keyId = try {
        JWT.decode(token).keyId
    } catch (_: JWTDecodeException) {
        rejectToken("malformed token")
        return null
    }
    val key = try {
        jwkProvider.get(keyId).publicKey as? RSAPublicKey
    } catch (e: JwkException) {
        rejectToken(jwkFailureReason(e))
        return null
    }
    if (key == null) {
        rejectToken("signing key is not RSA")
        return null
    }
    val verifier = JWT.require(Algorithm.RSA256(key, null))
        .withIssuer("https://securetoken.google.com/$projectId")
        .withAudience(projectId)
        .acceptLeeway(CLOCK_LEEWAY_SEC)
        .build()
    return LoggingVerifier(verifier)
}

// The cached provider wraps a fetch failure in SigningKeyNotFoundException; the cause chain tells them apart
private fun jwkFailureReason(e: JwkException): String {
    val chain = generateSequence<Throwable>(e) { it.cause }
    return when {
        chain.any { it is NetworkException } -> "JWKS unavailable"
        e is SigningKeyNotFoundException -> "unknown kid"
        else -> "JWKS unavailable"
    }
}

/** Why java-jwt refused a token, by exception type; the exception's message may quote the token and is not used. */
internal fun rejectionReason(e: JWTVerificationException): String = when (e) {
    is TokenExpiredException -> "expired"

    is IncorrectClaimException -> when (e.claimName) {
        "aud" -> "wrong audience"
        "iss" -> "wrong issuer"
        else -> "invalid claim ${e.claimName}"
    }

    is MissingClaimException -> "missing claim ${e.claimName}"

    is SignatureVerificationException -> "bad signature"

    is AlgorithmMismatchException -> "algorithm mismatch"

    is JWTDecodeException -> "malformed token"

    else -> "invalid token (${e::class.simpleName})"
}

private class LoggingVerifier(private val delegate: JWTVerifier) : JWTVerifier {
    override fun verify(token: String): DecodedJWT = logged { delegate.verify(token) }

    override fun verify(jwt: DecodedJWT): DecodedJWT = logged { delegate.verify(jwt) }

    private inline fun logged(verify: () -> DecodedJWT): DecodedJWT = try {
        verify()
    } catch (e: JWTVerificationException) {
        rejectToken(rejectionReason(e))
        throw e
    }
}

fun ApplicationCall.principalOrThrow(): FirebasePrincipal = principal<FirebasePrincipal>()
    ?: throw ApiException(HttpStatusCode.Unauthorized, ErrorCode.UNAUTHORIZED, "Unauthorized")
