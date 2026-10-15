package com.kturker.multiplatform.core.auth

import android.annotation.SuppressLint
import android.content.Context
import co.touchlab.kermit.Logger
import com.google.firebase.FirebaseApp
import org.koin.mp.KoinPlatform.getKoin

/**
 * Firebase starts itself (FirebaseInitProvider) from the `google_app_id` resource, which the google-services
 * plugin writes from google-services.json. Without that file the plugin is not applied (androidApp), there is
 * no resource and no Firebase app, and Firebase.auth would throw.
 */
internal class AndroidFirebaseAvailability(private val context: Context) : FirebaseAvailability {

    override val isConfigured: Boolean by lazy {
        val configured = FirebaseApp.getApps(context).isNotEmpty() ||
            (hasGoogleAppId() && FirebaseApp.initializeApp(context) != null)
        if (!configured) Logger.withTag("Auth").w { "No Firebase config (google-services.json): sign-in is off" }
        configured
    }

    // A library cannot reference the app's R class, so the resource is looked up by name
    @SuppressLint("DiscouragedApi")
    private fun hasGoogleAppId(): Boolean =
        context.resources.getIdentifier("google_app_id", "string", context.packageName) != 0
}

internal actual fun platformFirebaseAvailability(): FirebaseAvailability =
    AndroidFirebaseAvailability(context = getKoin().get())
