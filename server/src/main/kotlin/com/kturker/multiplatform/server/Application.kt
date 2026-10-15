@file:Suppress("MatchingDeclarationName") // Named after what the file sets up, not after one of its declarations.

package com.kturker.multiplatform.server

import com.auth0.jwk.JwkProvider
import com.kturker.multiplatform.server.auth.FIREBASE_AUTH
import com.kturker.multiplatform.server.auth.FirebasePrincipal
import com.kturker.multiplatform.server.auth.installFirebaseAuth
import com.kturker.multiplatform.server.db.FileRepository
import com.kturker.multiplatform.server.db.InviteRepository
import com.kturker.multiplatform.server.db.SpaceRepository
import com.kturker.multiplatform.server.db.UserRepository
import com.kturker.multiplatform.server.files.FileStore
import com.kturker.multiplatform.server.routes.accountRoutes
import com.kturker.multiplatform.server.routes.fileRoutes
import com.kturker.multiplatform.server.routes.healthRoutes
import com.kturker.multiplatform.server.routes.inviteRoutes
import com.kturker.multiplatform.server.routes.memberRoutes
import com.kturker.multiplatform.server.routes.profileRoutes
import com.kturker.multiplatform.server.routes.spaceRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import java.nio.file.Path

class AppDeps(val config: ServerConfig, val jwkProvider: JwkProvider, val db: Database?)

class Services(
    val users: UserRepository,
    val spaces: SpaceRepository,
    val invites: InviteRepository,
    val files: FileRepository,
    val store: FileStore
) {
    constructor(db: Database, store: FileStore) :
        this(UserRepository(db), SpaceRepository(db), InviteRepository(db), FileRepository(db), store)
}

// A path segment after one of these keys is a secret (an invite code) or an identifier (a uid): logged as a placeholder
private val REDACTED_AFTER = mapOf("invites" to "{token}", "members" to "{uid}")

/** [path] as the request log shows it: invite codes and member uids replaced with placeholders. */
internal fun redactedPath(path: String): String {
    val segments = path.split('/').toMutableList()
    for (i in 1 until segments.size) {
        REDACTED_AFTER[segments[i - 1]]?.let { segments[i] = it }
    }
    return segments.joinToString("/")
}

/**
 * Records (once per authenticated request) that the caller's Firebase user and personal space exist. Installed on
 * the authenticated routes below; internal so tests can put it on a test-only route.
 */
internal fun touchUsers(users: UserRepository) = createRouteScopedPlugin("TouchUsers") {
    on(AuthenticationChecked) { call ->
        call.principal<FirebasePrincipal>()?.let { users.touch(it.uid, it.email, it.name) }
    }
}

fun Application.module(deps: AppDeps) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
    // Status, method and redacted path only; never headers (tokens), bodies, query strings, invite codes or uids.
    install(CallLogging) {
        format { call ->
            "${call.response.status()}: ${call.request.httpMethod.value} - ${redactedPath(call.request.path())}"
        }
    }
    installErrors()
    installFirebaseAuth(deps)
    val services = deps.db?.let { Services(it, FileStore(Path.of(deps.config.filesDir))) }
    routing {
        healthRoutes()
        if (services != null) {
            authenticate(FIREBASE_AUTH) {
                install(touchUsers(services.users))
                spaceRoutes(services)
                memberRoutes(services)
                inviteRoutes(services)
                profileRoutes(services)
                fileRoutes(services)
                accountRoutes(services)
            }
        }
    }
}
