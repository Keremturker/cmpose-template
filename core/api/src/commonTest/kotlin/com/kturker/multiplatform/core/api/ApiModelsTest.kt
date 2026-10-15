package com.kturker.multiplatform.core.api

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ApiModelsTest {

    @Test
    fun roleAtLeastFollowsViewerEditorOwnerOrder() {
        val expected = mapOf(
            (Role.VIEWER to Role.VIEWER) to true,
            (Role.VIEWER to Role.EDITOR) to false,
            (Role.VIEWER to Role.OWNER) to false,
            (Role.EDITOR to Role.VIEWER) to true,
            (Role.EDITOR to Role.EDITOR) to true,
            (Role.EDITOR to Role.OWNER) to false,
            (Role.OWNER to Role.VIEWER) to true,
            (Role.OWNER to Role.EDITOR) to true,
            (Role.OWNER to Role.OWNER) to true,
        )
        assertEquals(Role.entries.size * Role.entries.size, expected.size)
        expected.forEach { (pair, result) ->
            val (role, other) = pair
            assertEquals(result, role.atLeast(other), "$role.atLeast($other)")
        }
    }

    @Test
    fun spaceRoundTripsThroughDefaultJson() {
        val space = Space(id = "s1", name = "Home", role = Role.EDITOR, isPersonal = false, memberCount = 3)
        val text = Json.encodeToString(space)
        assertEquals(
            """{"id":"s1","name":"Home","role":"editor","isPersonal":false,"memberCount":3}""",
            text,
        )
        assertEquals(space, Json.decodeFromString<Space>(text))
    }

    @Test
    fun invitePreviewRoundTripsThroughDefaultJson() {
        val preview = InvitePreview(spaceName = "Home", inviterName = null, role = Role.VIEWER, state = InviteState.USED)
        val text = Json.encodeToString(preview)
        assertEquals(
            """{"spaceName":"Home","inviterName":null,"role":"viewer","state":"used"}""",
            text,
        )
        assertEquals(preview, Json.decodeFromString<InvitePreview>(text))
    }

    @Test
    fun errorCodesUseStableWireNames() {
        val expected = mapOf(
            ErrorCode.UNAUTHORIZED to "unauthorized",
            ErrorCode.NOT_FOUND to "not_found",
            ErrorCode.BAD_REQUEST to "bad_request",
            ErrorCode.PAYLOAD_TOO_LARGE to "payload_too_large",
            ErrorCode.UNSUPPORTED_MEDIA_TYPE to "unsupported_media_type",
            ErrorCode.CONFLICT to "conflict",
            ErrorCode.EXPIRED to "expired",
            ErrorCode.INTERNAL to "internal",
            ErrorCode.UNAVAILABLE to "unavailable",
        )
        assertEquals(ErrorCode.entries.toSet(), expected.keys)
        expected.forEach { (code, wire) ->
            assertEquals("\"$wire\"", Json.encodeToString(code))
        }
        assertEquals(
            """{"error":"Not found","code":"not_found"}""",
            Json.encodeToString(ApiError("Not found", ErrorCode.NOT_FOUND)),
        )
    }

    @Test
    fun routesHaveNoLeadingSlash() {
        val routes = listOf(
            ApiRoutes.HEALTH,
            ApiRoutes.ME,
            ApiRoutes.PROFILE,
            ApiRoutes.SPACES,
            ApiRoutes.space("s1"),
            ApiRoutes.members("s1"),
            ApiRoutes.member("s1", "u1"),
            ApiRoutes.invites("s1"),
            ApiRoutes.invite("t1"),
            ApiRoutes.acceptInvite("t1"),
            ApiRoutes.files("s1"),
            ApiRoutes.file("f1"),
        )
        routes.forEach { assertFalse(it.startsWith("/"), "route '$it' starts with '/'") }
    }

    @Test
    fun routesBuildIdPaths() {
        assertEquals("spaces/s1", ApiRoutes.space("s1"))
        assertEquals("spaces/s1/members/u1", ApiRoutes.member("s1", "u1"))
        assertEquals("invites/t1/accept", ApiRoutes.acceptInvite("t1"))
        assertEquals("spaces/s1/files", ApiRoutes.files("s1"))
        assertEquals("files/f1", ApiRoutes.file("f1"))
    }
}
