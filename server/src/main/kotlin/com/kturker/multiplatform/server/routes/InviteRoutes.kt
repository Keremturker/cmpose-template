package com.kturker.multiplatform.server.routes

import com.kturker.multiplatform.core.api.ApiRoutes
import com.kturker.multiplatform.core.api.ErrorCode
import com.kturker.multiplatform.core.api.InviteRequest
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.server.ApiException
import com.kturker.multiplatform.server.Services
import com.kturker.multiplatform.server.access.requireRole
import com.kturker.multiplatform.server.auth.principalOrThrow
import com.kturker.multiplatform.server.badRequest
import com.kturker.multiplatform.server.db.AcceptResult
import com.kturker.multiplatform.server.notFound
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/** The owner creates invite links; any signed-in user previews and accepts one. */
fun Route.inviteRoutes(services: Services) {
    post("/" + ApiRoutes.invites("{id}")) {
        val membership = call.requireRole(services.spaces, call.parameters["id"].orEmpty(), Role.OWNER)
        val role = call.receive<InviteRequest>().role
        if (role == Role.OWNER) badRequest("Owner role cannot be invited")
        call.respond(HttpStatusCode.Created, services.invites.create(membership.spaceId, membership.uid, role))
    }
    get("/" + ApiRoutes.invite("{token}")) {
        val (preview, _) = services.invites.preview(call.parameters["token"].orEmpty()) ?: notFound()
        call.respond(preview)
    }
    post("/" + ApiRoutes.acceptInvite("{token}")) {
        val uid = call.principalOrThrow().uid
        when (val result = services.invites.accept(call.parameters["token"].orEmpty(), uid)) {
            is AcceptResult.Joined -> call.respond(services.spaces.get(result.spaceId, uid) ?: notFound())
            AcceptResult.Used -> throw ApiException(HttpStatusCode.Conflict, ErrorCode.CONFLICT, "Invite already used")
            AcceptResult.Expired -> throw ApiException(HttpStatusCode.Gone, ErrorCode.EXPIRED, "Invite expired")
            AcceptResult.NotFound -> notFound()
        }
    }
}
