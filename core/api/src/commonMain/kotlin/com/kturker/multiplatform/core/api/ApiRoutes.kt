package com.kturker.multiplatform.core.api

/**
 * Paths shared by client and server, relative and without a leading `/`: the client's base URL
 * ends with `/`, and the server mounts each route as `"/" + path`.
 */
object ApiRoutes {
    const val HEALTH = "health"
    const val ME = "me"
    const val PROFILE = "me/profile"
    const val SPACES = "spaces"

    fun space(id: String) = "spaces/$id"
    fun members(spaceId: String) = "spaces/$spaceId/members"
    fun member(
        spaceId: String,
        uid: String
    ) = "spaces/$spaceId/members/$uid"
    fun invites(spaceId: String) = "spaces/$spaceId/invites"
    fun invite(token: String) = "invites/$token"
    fun acceptInvite(token: String) = "invites/$token/accept"
    fun files(spaceId: String) = "spaces/$spaceId/files"
    fun file(id: String) = "files/$id"
}
