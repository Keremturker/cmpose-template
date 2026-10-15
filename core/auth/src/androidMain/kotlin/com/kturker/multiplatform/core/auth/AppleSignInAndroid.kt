package com.kturker.multiplatform.core.auth

import android.app.Activity
import co.touchlab.kermit.Logger
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.tasks.await

private val log = Logger.withTag("AppleSignIn")

// The user closed the browser tab Firebase opened for Apple's sign-in page
private const val WEB_CONTEXT_CANCELED = "ERROR_WEB_CONTEXT_CANCELED"

/**
 * Sign in with Apple on Android: Android has no native Apple sign-in, so Firebase runs Apple's web flow
 * in a Custom Tab and signs the user in itself when it returns.
 */
internal class AppleSignInAndroid {

    suspend fun signIn(activity: Activity): SocialToken {
        val provider = OAuthProvider.newBuilder(APPLE_PROVIDER)
            .setScopes(listOf("email", "name"))
            .build()
        return try {
            FirebaseAuth.getInstance().startActivityForSignInWithProvider(activity, provider).await()
            SocialToken.SignedInByPlatform
        } catch (e: FirebaseAuthException) {
            if (e.errorCode == WEB_CONTEXT_CANCELED) {
                SocialToken.Cancelled
            } else {
                log.w { "Apple sign-in refused: ${e.errorCode}" }
                SocialToken.Failed(authFailureOf(e.errorCode))
            }
        } catch (_: FirebaseNetworkException) {
            SocialToken.Failed(AuthFailure.NETWORK)
        }
    }
}
