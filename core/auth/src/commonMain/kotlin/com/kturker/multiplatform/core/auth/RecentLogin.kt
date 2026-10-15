package com.kturker.multiplatform.core.auth

/** Firebase refuses account deletion when the last sign-in is older than this (its own threshold). */
internal const val RECENT_LOGIN_SECONDS = 5 * 60L

/**
 * A timestamp claim of the ID token in epoch seconds (`iat`, `auth_time`). Android's claims hold a number;
 * on iOS the value may arrive as an NSNumber, whose string form is the number.
 */
internal fun claimSeconds(
    claims: Map<String, Any>,
    name: String
): Long? = when (val value = claims[name]) {
    null -> null
    is Number -> value.toLong()
    else -> value.toString().toDoubleOrNull()?.toLong()
}

/**
 * Whether the session is recent enough for Firebase to delete the account, read from a just-refreshed ID
 * token: `iat` (issued now, by the server) minus `auth_time` (the last sign-in). Both are server timestamps,
 * so the device clock, which may run fast or slow, plays no part. A missing claim errs on the safe side.
 */
internal fun recentLoginResult(claims: Map<String, Any>): AuthResult {
    val issuedAt = claimSeconds(claims, "iat") ?: return AuthResult.NeedsRecentLogin
    val authTime = claimSeconds(claims, "auth_time") ?: return AuthResult.NeedsRecentLogin
    return if (issuedAt - authTime < RECENT_LOGIN_SECONDS) AuthResult.Success else AuthResult.NeedsRecentLogin
}

/**
 * A failed forced token refresh: an expired or revoked session (Android ERROR_USER_TOKEN_EXPIRED /
 * ERROR_INVALID_USER_TOKEN, iOS 17021 / 17017) is cured by signing in again, like requires-recent-login.
 */
internal fun refreshResultOf(code: String?): AuthResult = when (code) {
    "ERROR_USER_TOKEN_EXPIRED", "ERROR_INVALID_USER_TOKEN", "17021", "17017" -> AuthResult.NeedsRecentLogin
    else -> authResultOf(code)
}

/**
 * A failed `user.delete()`: requires-recent-login, and also a revoked or expired session, ask for a fresh
 * sign-in, the same answers [refreshResultOf] gives canDeleteNow.
 */
internal fun deleteUserResultOf(code: String?): AuthResult = refreshResultOf(code)
