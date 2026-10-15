package com.kturker.multiplatform.core.auth

/** Platform half of social sign-in: returns a Firebase credential source or a cancel/failure. */
interface SocialSignIn {
    suspend fun google(): SocialToken
    suspend fun apple(): SocialToken
}

sealed interface SocialToken {
    data class Google(val idToken: String, val accessToken: String?) : SocialToken
    data class Apple(val idToken: String, val rawNonce: String) : SocialToken

    /** Android Apple: Firebase finished the web flow itself; the user is already signed in. */
    data object SignedInByPlatform : SocialToken
    data object Cancelled : SocialToken
    data class Failed(val reason: AuthFailure) : SocialToken
}

/** Android: Credential Manager and Firebase's Apple web flow; iOS: the Swift [IosSignInBridge]. */
internal expect fun platformSocialSignIn(): SocialSignIn
