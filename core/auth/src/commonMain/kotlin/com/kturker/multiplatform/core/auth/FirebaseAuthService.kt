package com.kturker.multiplatform.core.auth

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.FirebaseTooManyRequestsException
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.auth.code
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val log = Logger.withTag("Auth")

/** [AuthService] over Firebase Auth (GitLive's common API); social sign-in's platform half is [social]. */
internal class FirebaseAuthService(
    private val auth: FirebaseAuth,
    private val social: SocialSignIn,
    scope: CoroutineScope
) : AuthService {

    private val notice = SessionNotice()

    override val state: StateFlow<AuthState> = auth.authStateChanged
        .map { user -> if (user == null) AuthState.SignedOut else AuthState.SignedIn(user.uid, user.email) }
        .stateIn(scope, SharingStarted.Eagerly, AuthState.Loading)

    override fun currentUid(): String? = auth.currentUser?.uid

    override fun currentDisplayName(): String? = auth.currentUser?.displayName

    override suspend fun token(forceRefresh: Boolean): String? = try {
        auth.currentUser?.getIdToken(forceRefresh)
    } catch (e: FirebaseException) {
        // No token: the API call goes out unauthenticated and the server answers 401
        log.w { "No ID token: ${e::class.simpleName}" }
        null
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): AuthResult = firebaseCall {
        auth.signInWithEmailAndPassword(email, password)
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String
    ): AuthResult = firebaseCall {
        auth.createUserWithEmailAndPassword(email, password)
    }

    // The platform half may throw more than Firebase's types (Credential Manager, Firebase's Apple web flow):
    // inside firebaseResult every such exception becomes a failure instead of a crash
    override suspend fun signInWithGoogle(): AuthResult =
        socialSignInResult(social::google, ::signInWith)

    override suspend fun signInWithApple(): AuthResult =
        socialSignInResult(social::apple, ::signInWith)

    override suspend fun signOut(reason: SignOutReason) {
        if (reason == SignOutReason.SESSION_ENDED) notice.post()
        auth.signOut()
    }

    override fun takeSessionEndedNotice(): Boolean = notice.take()

    override suspend fun deleteUser(): AuthResult {
        val user = auth.currentUser ?: return AuthResult.Success
        return firebaseCall(::deleteUserResultOf) { user.delete() }
    }

    override suspend fun canDeleteNow(): AuthResult {
        // Without a user only a fresh sign-in helps
        val user = auth.currentUser ?: return AuthResult.NeedsRecentLogin
        // A revoked or expired session makes the forced refresh fail (refreshResultOf); a live one gets a
        // token whose iat and auth_time, both from the server, tell how old the sign-in is
        return firebaseResult(::refreshResultOf) {
            user.reload()
            recentLoginResult(user.getIdTokenResult(forceRefresh = true).claims)
        }
    }

    private suspend fun signInWith(token: SocialToken): AuthResult = when (token) {
        is SocialToken.Google -> credentialSignIn(GoogleAuthProvider.credential(token.idToken, token.accessToken))

        is SocialToken.Apple -> credentialSignIn(
            OAuthProvider.credential(providerId = APPLE_PROVIDER, idToken = token.idToken, rawNonce = token.rawNonce)
        )

        SocialToken.SignedInByPlatform -> AuthResult.Success

        SocialToken.Cancelled -> AuthResult.Cancelled

        is SocialToken.Failed -> AuthResult.Failure(token.reason)
    }

    private suspend fun credentialSignIn(credential: AuthCredential): AuthResult = firebaseCall(::socialResultOf) {
        auth.signInWithCredential(credential)
    }

    private suspend fun firebaseCall(
        resultOf: (code: String?) -> AuthResult = ::authResultOf,
        block: suspend () -> Unit
    ): AuthResult = firebaseResult(resultOf) {
        block()
        AuthResult.Success
    }
}

/** Social sign-in: the platform's token, then Firebase's sign-in with it, both inside [firebaseResult]. */
internal suspend fun socialSignInResult(
    platformToken: suspend () -> SocialToken,
    signIn: suspend (SocialToken) -> AuthResult
): AuthResult = firebaseResult { signIn(platformToken()) }

// Firebase's errors, and any other exception, become results; the caller never sees an exception
@Suppress("TooGenericExceptionCaught")
internal suspend fun firebaseResult(
    resultOf: (code: String?) -> AuthResult = ::authResultOf,
    block: suspend () -> AuthResult
): AuthResult = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: FirebaseAuthException) {
    log.w { "Firebase Auth refused: ${e.code}" }
    resultOf(e.code)
} catch (_: FirebaseNetworkException) {
    AuthResult.Failure(AuthFailure.NETWORK)
} catch (_: FirebaseTooManyRequestsException) {
    AuthResult.Failure(AuthFailure.UNAVAILABLE)
} catch (e: Exception) {
    log.e(e) { "Firebase Auth call failed" }
    AuthResult.Failure(AuthFailure.UNKNOWN)
}

internal const val APPLE_PROVIDER = "apple.com"
