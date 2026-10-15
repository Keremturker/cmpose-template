package com.kturker.multiplatform.core.auth

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Whether Firebase will still accept account deletion is read from the refreshed token alone: `iat` (when the
 * token was issued) minus `auth_time` (when the user signed in), both set by Firebase's servers. The device
 * clock never takes part, so a device that runs fast or slow gets the same answer.
 */
class RecentLoginTest {

    private val serverNow = 1_800_000_000L

    private fun claims(issuedAt: Long, authTime: Long): Map<String, Any> = mapOf("iat" to issuedAt, "auth_time" to authTime)

    @Test
    fun aFreshSignInIsRecent() {
        assertEquals(AuthResult.Success, recentLoginResult(claims(issuedAt = serverNow, authTime = serverNow - 60)))
        assertEquals(AuthResult.Success, recentLoginResult(claims(issuedAt = serverNow, authTime = serverNow - 299)))
    }

    @Test
    fun anOldSignInNeedsAFreshOne() {
        assertEquals(
            AuthResult.NeedsRecentLogin,
            recentLoginResult(claims(issuedAt = serverNow, authTime = serverNow - 301))
        )
    }

    // A device clock an hour fast or slow cannot move the answer: only the server's two timestamps count
    @Test
    fun aSkewedDeviceClockDoesNotChangeTheOutcome() {
        val hour = 3_600L
        listOf(serverNow - hour, serverNow, serverNow + hour).forEach { deviceNow ->
            // The check has no clock input at all; the same token claims give the same result whatever deviceNow is
            val fresh = claims(issuedAt = serverNow, authTime = serverNow - 60)
            val old = claims(issuedAt = serverNow, authTime = serverNow - 600)
            assertEquals(AuthResult.Success, recentLoginResult(fresh), "device clock $deviceNow")
            assertEquals(AuthResult.NeedsRecentLogin, recentLoginResult(old), "device clock $deviceNow")
        }
    }

    @Test
    fun missingClaimsNeedAFreshSignIn() {
        assertEquals(AuthResult.NeedsRecentLogin, recentLoginResult(emptyMap()))
        assertEquals(AuthResult.NeedsRecentLogin, recentLoginResult(mapOf("auth_time" to serverNow)))
        assertEquals(AuthResult.NeedsRecentLogin, recentLoginResult(mapOf("iat" to serverNow)))
    }

    // Android's claims hold numbers, iOS's may hold an NSNumber whose string form is the number
    @Test
    fun timestampsAreReadFromNumbersAndTheirStringForm() {
        assertEquals(serverNow, claimSeconds(mapOf("auth_time" to serverNow), "auth_time"))
        assertEquals(serverNow, claimSeconds(mapOf("iat" to serverNow.toString()), "iat"))
        assertEquals(null, claimSeconds(emptyMap(), "iat"))
        assertEquals(
            AuthResult.Success,
            recentLoginResult(mapOf("iat" to serverNow.toString(), "auth_time" to (serverNow - 10).toInt()))
        )
    }

    // A forced refresh on a revoked or expired session fails; the remedy is signing in again
    @Test
    fun aRevokedSessionOnRefreshNeedsAFreshSignIn() {
        listOf("ERROR_USER_TOKEN_EXPIRED", "ERROR_INVALID_USER_TOKEN", "17021", "17017", "ERROR_REQUIRES_RECENT_LOGIN")
            .forEach { assertEquals(AuthResult.NeedsRecentLogin, refreshResultOf(it), it) }
        assertEquals(AuthResult.Failure(AuthFailure.NETWORK), refreshResultOf("ERROR_NETWORK_REQUEST_FAILED"))
    }

    // user.delete() on a revoked or expired session fails like the forced refresh: a fresh sign-in cures it
    @Test
    fun aRevokedSessionOnDeleteNeedsAFreshSignIn() {
        val codes = listOf(
            "ERROR_USER_TOKEN_EXPIRED", "ERROR_INVALID_USER_TOKEN", "17021", "17017", "ERROR_REQUIRES_RECENT_LOGIN", "17014"
        )
        codes.forEach { assertEquals(AuthResult.NeedsRecentLogin, deleteUserResultOf(it), it) }
        assertEquals(AuthResult.Failure(AuthFailure.NETWORK), deleteUserResultOf("17020"))
    }
}
