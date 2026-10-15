package com.kturker.multiplatform.server.routes

import com.kturker.multiplatform.core.api.ApiRoutes
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.RoleChange
import com.kturker.multiplatform.server.Services
import com.kturker.multiplatform.server.access.requireRole
import com.kturker.multiplatform.server.auth.principalOrThrow
import com.kturker.multiplatform.server.badRequest
import com.kturker.multiplatform.server.notFound
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch

/**
 * Members of a space: any member lists them; the owner changes roles and removes members; anyone but the owner may
 * leave. There is no ownership transfer: the owner's role never changes and OWNER is never granted.
 */
fun Route.memberRoutes(services: Services) {
    get("/" + ApiRoutes.members("{id}")) {
        val membership = call.requireRole(services.spaces, call.parameters["id"].orEmpty(), Role.VIEWER)
        call.respond(services.spaces.members(membership.spaceId))
    }
    patch("/" + ApiRoutes.member("{id}", "{uid}")) {
        val membership = call.requireRole(services.spaces, call.parameters["id"].orEmpty(), Role.OWNER)
        val target = call.parameters["uid"].orEmpty()
        val role = call.receive<RoleChange>().role
        if (target == membership.uid) badRequest("Owner cannot change their own role")
        if (role == Role.OWNER) badRequest("Owner role cannot be granted")
        if (!services.spaces.setRole(membership.spaceId, target, role)) notFound()
        call.respond(HttpStatusCode.NoContent)
    }
    delete("/" + ApiRoutes.member("{id}", "{uid}")) {
        val spaceId = call.parameters["id"].orEmpty()
        val target = call.parameters["uid"].orEmpty()
        val leaving = target == call.principalOrThrow().uid
        val membership = call.requireRole(services.spaces, spaceId, if (leaving) Role.VIEWER else Role.OWNER)
        if (leaving && membership.role == Role.OWNER) badRequest("Owner cannot leave; delete the space")
        if (!services.spaces.removeMember(membership.spaceId, target)) notFound()
        call.respond(HttpStatusCode.NoContent)
    }
}
