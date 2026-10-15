@file:Suppress("MatchingDeclarationName") // Named after what the file sets up, not after one of its declarations.

package com.kturker.multiplatform.server.access

import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.atLeast
import com.kturker.multiplatform.server.auth.principalOrThrow
import com.kturker.multiplatform.server.db.SpaceRepository
import com.kturker.multiplatform.server.notFound
import io.ktor.server.application.ApplicationCall
import java.util.UUID

data class Membership(val spaceId: UUID, val uid: String, val role: Role)

/** [id] as a UUID, or null when it is not one (a path id that is no UUID answers 404, not 500). */
fun uuidOrNull(id: String): UUID? = runCatching { UUID.fromString(id) }.getOrNull()

/**
 * The caller's membership in [spaceId] if it is at least [atLeast]; otherwise 404 (never 403: a space a caller
 * cannot see must look the same as one that does not exist).
 */
fun ApplicationCall.requireRole(
    spaces: SpaceRepository,
    spaceId: String,
    atLeast: Role
): Membership {
    val id = uuidOrNull(spaceId) ?: notFound()
    val membership = spaces.membership(id, principalOrThrow().uid) ?: notFound()
    if (!membership.role.atLeast(atLeast)) notFound()
    return membership
}
