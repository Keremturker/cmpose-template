package com.kturker.multiplatform.core.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    @SerialName("owner")
    OWNER,

    @SerialName("editor")
    EDITOR,

    @SerialName("viewer")
    VIEWER
}

/** Role order for checks: VIEWER < EDITOR < OWNER. */
fun Role.atLeast(other: Role): Boolean = rank() >= other.rank()

private fun Role.rank() = when (this) {
    Role.VIEWER -> 0
    Role.EDITOR -> 1
    Role.OWNER -> 2
}

@Serializable
data class Space(val id: String, val name: String, val role: Role, val isPersonal: Boolean, val memberCount: Int)

@Serializable
data class SpaceDraft(val name: String)

@Serializable
data class Member(val uid: String, val displayName: String?, val email: String?, val role: Role, val joinedAt: Long)

@Serializable
data class RoleChange(val role: Role)

@Serializable
data class InviteRequest(val role: Role)

@Serializable
data class InviteLink(val token: String, val expiresAt: Long)

@Serializable
enum class InviteState {
    @SerialName("valid")
    VALID,

    @SerialName("used")
    USED,

    @SerialName("expired")
    EXPIRED
}

@Serializable
data class InvitePreview(val spaceName: String, val inviterName: String?, val role: Role, val state: InviteState)

@Serializable
data class UploadResult(val fileId: String)

@Serializable
data class Profile(val displayName: String? = null, val personalSpaceName: String)

@Serializable
data class ApiError(val error: String, val code: ErrorCode)

@Serializable
enum class ErrorCode {
    @SerialName("unauthorized")
    UNAUTHORIZED,

    @SerialName("not_found")
    NOT_FOUND,

    @SerialName("bad_request")
    BAD_REQUEST,

    @SerialName("payload_too_large")
    PAYLOAD_TOO_LARGE,

    @SerialName("unsupported_media_type")
    UNSUPPORTED_MEDIA_TYPE,

    @SerialName("conflict")
    CONFLICT,

    /** A link that has run out, such as an expired invite (HTTP 410). */
    @SerialName("expired")
    EXPIRED,

    @SerialName("internal")
    INTERNAL,

    @SerialName("unavailable")
    UNAVAILABLE
}
