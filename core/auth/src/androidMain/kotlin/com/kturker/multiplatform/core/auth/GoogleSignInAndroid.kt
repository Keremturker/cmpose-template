package com.kturker.multiplatform.core.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import co.touchlab.kermit.Logger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

private val log = Logger.withTag("GoogleSignIn")

/** Google sign-in through Credential Manager: the account picker returns a Google ID token for Firebase. */
internal class GoogleSignInAndroid(private val context: Context) {

    suspend fun signIn(activity: Activity): SocialToken {
        val clientId = webClientId() ?: run {
            log.e { "default_web_client_id is missing: google-services.json was not applied" }
            return SocialToken.Failed(AuthFailure.UNAVAILABLE)
        }
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(clientId)
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        return try {
            val credential = CredentialManager.create(activity).getCredential(activity, request).credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                SocialToken.Google(GoogleIdTokenCredential.createFrom(credential.data).idToken, accessToken = null)
            } else {
                log.w { "Unexpected credential type: ${credential.type}" }
                SocialToken.Failed(AuthFailure.UNKNOWN)
            }
        } catch (_: GetCredentialCancellationException) {
            SocialToken.Cancelled
        } catch (e: NoCredentialException) {
            // No Google account on the device (or Play services cannot offer one)
            log.w { "No Google credential: ${e.type}" }
            SocialToken.Failed(AuthFailure.UNAVAILABLE)
        } catch (e: GetCredentialException) {
            log.w { "Google sign-in failed: ${e.type}" }
            SocialToken.Failed(AuthFailure.UNKNOWN)
        } catch (e: GoogleIdTokenParsingException) {
            log.e(e) { "Google ID token could not be read" }
            SocialToken.Failed(AuthFailure.UNKNOWN)
        }
    }

    // The google-services plugin writes the OAuth web client ID into the app's resources; a library
    // cannot reference the app's R class, so it is looked up by name.
    @SuppressLint("DiscouragedApi")
    private fun webClientId(): String? {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (id == 0) null else context.getString(id)
    }
}
