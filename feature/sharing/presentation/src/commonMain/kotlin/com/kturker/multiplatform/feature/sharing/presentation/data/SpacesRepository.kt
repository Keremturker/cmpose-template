package com.kturker.multiplatform.feature.sharing.presentation.data

import com.kturker.multiplatform.core.api.ApiRoutes
import com.kturker.multiplatform.core.api.InviteLink
import com.kturker.multiplatform.core.api.InvitePreview
import com.kturker.multiplatform.core.api.InviteRequest
import com.kturker.multiplatform.core.api.Member
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.RoleChange
import com.kturker.multiplatform.core.api.Space
import com.kturker.multiplatform.core.api.SpaceDraft
import com.kturker.multiplatform.core.domain.RestResult
import com.kturker.multiplatform.core.network.BaseRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * The backend's spaces, members and invite calls. A space the user is not a member of answers 404, like one that
 * does not exist; accepting a used invite answers 409, an expired one 410.
 */
interface SpacesRepository {
    /** GET /spaces: every space the user is a member of, their personal space included. */
    suspend fun list(): RestResult<List<Space>>
    suspend fun create(name: String): RestResult<Space>
    suspend fun rename(
        id: String,
        name: String
    ): RestResult<Space>
    suspend fun delete(id: String): RestResult<Unit>
    suspend fun members(id: String): RestResult<List<Member>>
    suspend fun setRole(
        id: String,
        uid: String,
        role: Role
    ): RestResult<Unit>

    /** Removes [uid] from the space; the user's own uid leaves it. */
    suspend fun remove(
        id: String,
        uid: String
    ): RestResult<Unit>
    suspend fun invite(
        id: String,
        role: Role
    ): RestResult<InviteLink>
    suspend fun preview(token: String): RestResult<InvitePreview>

    /** Joins the invite's space; an existing member gets the space back and the invite stays unused. */
    suspend fun accept(token: String): RestResult<Space>
}

/** [SpacesRepository] over the backend client (base URL, bearer token, JSON; a 401 refreshes the token once). */
internal class KtorSpacesRepository(private val client: HttpClient) :
    BaseRepository(),
    SpacesRepository {

    override suspend fun list(): RestResult<List<Space>> = request { client.get(ApiRoutes.SPACES) }

    override suspend fun create(name: String): RestResult<Space> =
        request { client.post(ApiRoutes.SPACES) { json(SpaceDraft(name)) } }

    override suspend fun rename(
        id: String,
        name: String
    ): RestResult<Space> = request { client.patch(ApiRoutes.space(id)) { json(SpaceDraft(name)) } }

    override suspend fun delete(id: String): RestResult<Unit> =
        request { client.delete(ApiRoutes.space(id)) }

    override suspend fun members(id: String): RestResult<List<Member>> =
        request { client.get(ApiRoutes.members(id)) }

    override suspend fun setRole(
        id: String,
        uid: String,
        role: Role
    ): RestResult<Unit> =
        request { client.patch(ApiRoutes.member(id, uid)) { json(RoleChange(role)) } }

    override suspend fun remove(
        id: String,
        uid: String
    ): RestResult<Unit> = request { client.delete(ApiRoutes.member(id, uid)) }

    override suspend fun invite(
        id: String,
        role: Role
    ): RestResult<InviteLink> =
        request { client.post(ApiRoutes.invites(id)) { json(InviteRequest(role)) } }

    override suspend fun preview(token: String): RestResult<InvitePreview> =
        request { client.get(ApiRoutes.invite(token)) }

    override suspend fun accept(token: String): RestResult<Space> =
        request { client.post(ApiRoutes.acceptInvite(token)) }

    private inline fun <reified T : Any> HttpRequestBuilder.json(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
}
