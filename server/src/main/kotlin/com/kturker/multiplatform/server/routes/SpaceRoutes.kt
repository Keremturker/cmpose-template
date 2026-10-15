package com.kturker.multiplatform.server.routes

import com.kturker.multiplatform.core.api.ApiRoutes
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.SpaceDraft
import com.kturker.multiplatform.server.Services
import com.kturker.multiplatform.server.access.requireRole
import com.kturker.multiplatform.server.auth.principalOrThrow
import com.kturker.multiplatform.server.badRequest
import com.kturker.multiplatform.server.db.MAX_SPACE_NAME
import com.kturker.multiplatform.server.notFound
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

/** [name] trimmed, or 400 when it is blank or longer than [MAX_SPACE_NAME]. */
internal fun spaceName(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty() || trimmed.length > MAX_SPACE_NAME) badRequest("Name must be 1 to $MAX_SPACE_NAME characters")
    return trimmed
}

/** List, create, rename and delete spaces. */
fun Route.spaceRoutes(services: Services) {
    get("/" + ApiRoutes.SPACES) {
        call.respond(services.spaces.listFor(call.principalOrThrow().uid))
    }
    post("/" + ApiRoutes.SPACES) {
        val name = spaceName(call.receive<SpaceDraft>().name)
        call.respond(HttpStatusCode.Created, services.spaces.create(call.principalOrThrow().uid, name))
    }
    patch("/" + ApiRoutes.space("{id}")) {
        val membership = call.requireRole(services.spaces, call.parameters["id"].orEmpty(), Role.OWNER)
        val name = spaceName(call.receive<SpaceDraft>().name)
        services.spaces.rename(membership.spaceId, name)
        call.respond(services.spaces.get(membership.spaceId, membership.uid) ?: notFound())
    }
    delete("/" + ApiRoutes.space("{id}")) {
        val membership = call.requireRole(services.spaces, call.parameters["id"].orEmpty(), Role.OWNER)
        if (services.spaces.isPersonal(membership.spaceId)) badRequest("Personal space cannot be deleted")
        call.removeFiles(services.store, services.spaces.delete(membership.spaceId))
        call.removeSpaceDirs(services.store, listOf(membership.spaceId))
        call.respond(HttpStatusCode.NoContent)
    }
}
