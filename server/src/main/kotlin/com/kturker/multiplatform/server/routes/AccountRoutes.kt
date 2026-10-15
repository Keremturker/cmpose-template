package com.kturker.multiplatform.server.routes

import com.kturker.multiplatform.core.api.ApiRoutes
import com.kturker.multiplatform.server.Services
import com.kturker.multiplatform.server.auth.principalOrThrow
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete

/**
 * Account deletion (App Store rule 5.1.1(v)); the client then deletes the Firebase user. Spaces the caller owns go
 * with everything in them; in other people's spaces only the caller's membership goes, and the space stays. Calling
 * it again (the app retries after Firebase asks for a fresh sign-in) finds nothing left and is still 204.
 */
fun Route.accountRoutes(services: Services) {
    delete("/" + ApiRoutes.ME) {
        val uid = call.principalOrThrow().uid
        // Owned spaces first, collecting their file paths: CASCADE removes their members, invites and file rows
        val owned = services.spaces.ownedBy(uid)
        val paths = owned.flatMap { services.spaces.delete(it) }
        services.invites.deleteCreatedBy(uid)
        // CASCADE removes the caller's memberships in other people's spaces
        services.users.delete(uid)
        // Rows are gone; a file left on disk is only logged (by file id, never the uid) so deletion still succeeds.
        // The emptied folders (and any file the rows no longer knew about) go with their spaces.
        call.removeFiles(services.store, paths)
        call.removeSpaceDirs(services.store, owned)
        call.respond(HttpStatusCode.NoContent)
    }
}
