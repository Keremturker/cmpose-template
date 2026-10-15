package com.kturker.multiplatform.core.auth

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * The most recently resumed Activity, held weakly: Credential Manager and Firebase's Apple flow need an
 * Activity to show their UI. The app's Application installs it before Koin starts.
 */
object CurrentActivity {

    private var current: WeakReference<Activity>? = null
    private var installed = false

    val activity: Activity?
        get() = current?.get()

    fun install(application: Application) {
        if (installed) return
        installed = true
        application.registerActivityLifecycleCallbacks(Callbacks)
    }

    private object Callbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            current = WeakReference(activity)
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (current?.get() === activity) current = null
        }

        override fun onActivityCreated(
            activity: Activity,
            savedInstanceState: Bundle?
        ) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(
            activity: Activity,
            outState: Bundle
        ) = Unit
    }
}
