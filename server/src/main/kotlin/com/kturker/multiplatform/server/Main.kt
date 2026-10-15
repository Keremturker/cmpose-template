package com.kturker.multiplatform.server

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.kturker.multiplatform.server.db.connectDatabase
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.net.URI
import java.util.concurrent.TimeUnit

private const val GOOGLE_JWKS =
    "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com"
private const val JWKS_CACHE_SIZE = 10L
private const val JWKS_CACHE_HOURS = 6L
private const val JWKS_FETCHES_PER_MINUTE = 10L

/** Google's signing keys for Firebase ID tokens, cached and rate limited (also used by the local runner). */
fun googleJwkProvider(): JwkProvider = JwkProviderBuilder(URI(GOOGLE_JWKS).toURL())
    .cached(JWKS_CACHE_SIZE, JWKS_CACHE_HOURS, TimeUnit.HOURS)
    .rateLimited(JWKS_FETCHES_PER_MINUTE, 1, TimeUnit.MINUTES)
    .build()

fun main() {
    val config = ServerConfig.fromEnv()
    val db = connectDatabase(config)
    embeddedServer(Netty, port = config.port) { module(AppDeps(config, googleJwkProvider(), db)) }.start(wait = true)
}
