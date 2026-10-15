package com.kturker.multiplatform.core.auth

import com.kturker.multiplatform.core.network.TokenSource
import kotlinx.coroutines.flow.StateFlow

/** The signed-in user and the ways to sign in and out; its ID token authenticates API calls ([TokenSource]). */
interface AuthService : TokenSource {
    val state: StateFlow<AuthState>

    /**
     * The uid Firebase holds right now, read synchronously. [state] follows Firebase's listener and may still
     * show the previous user just after a sign-in returns.
     */
    fun currentUid(): String?

    /** The signed-in user's display name as Firebase holds it right now (Google and Apple fill it in), or null. */
    fun currentDisplayName(): String?
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): AuthResult
    suspend fun signUpWithEmail(
        email: String,
        password: String
    ): AuthResult
    suspend fun signInWithGoogle(): AuthResult
    suspend fun signInWithApple(): AuthResult

    /** Signs out; [SignOutReason.SESSION_ENDED] leaves a notice the next sign-in screen shows once. */
    suspend fun signOut(reason: SignOutReason = SignOutReason.USER)

    /** True once after a [SignOutReason.SESSION_ENDED] sign-out; the sign-in screen asks when it opens. */
    fun takeSessionEndedNotice(): Boolean

    /** Deletes the Firebase user; [AuthResult.NeedsRecentLogin] when Firebase wants a fresh sign-in first. */
    suspend fun deleteUser(): AuthResult

    /** Proves the session is recent enough to delete the account, without deleting anything. */
    suspend fun canDeleteNow(): AuthResult
}
