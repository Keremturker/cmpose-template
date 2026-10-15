package com.kturker.multiplatform.server.routes

import com.kturker.multiplatform.core.api.ApiRoutes
import com.kturker.multiplatform.core.api.Profile
import com.kturker.multiplatform.server.Services
import com.kturker.multiplatform.server.auth.principalOrThrow
import com.kturker.multiplatform.server.badRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.put

/** Longest display name after trimming. */
const val MAX_DISPLAY_NAME = 80

/**
 * The caller's display name and the localized name of their personal space. A display name set here wins over the
 * token's `name` claim on later logins; a null one leaves the stored name as it is. The personal space takes the
 * localized name only while it still has the server's default one, so a name the owner chose survives later syncs.
 */
fun Route.profileRoutes(services: Services) {
    put("/" + ApiRoutes.PROFILE) {
        val uid = call.principalOrThrow().uid
        val profile = call.receive<Profile>()
        val displayName = profile.displayName?.trim()?.also {
            if (it.isEmpty() || it.length > MAX_DISPLAY_NAME) {
                badRequest("Display name must be 1 to $MAX_DISPLAY_NAME characters")
            }
        }
        val personalName = spaceName(profile.personalSpaceName)
        displayName?.let { services.users.setDisplayName(uid, it) }
        services.spaces.localizePersonal(uid, personalName)
        call.respond(HttpStatusCode.NoContent)
    }
}
