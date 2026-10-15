package com.kturker.multiplatform.core.auth

/**
 * Drops the backend client's cached bearer token, so the next request asks Firebase for the current user's.
 * AuthModule binds it to the backend client's forgetToken(); a ViewModel that signs in again mid-flow calls it.
 */
fun interface SessionTokens {
    fun forget()
}

/**
 * Signs out and drops the cached bearer token with the session, so the next user's calls never carry this one's.
 * A backend call still answered 401 after the token refresh ends the session with [SignOutReason.SESSION_ENDED].
 */
suspend fun AuthService.signOutAndForget(
    tokens: SessionTokens,
    reason: SignOutReason
) {
    signOut(reason)
    tokens.forget()
}
