package com.kturker.multiplatform

import android.app.Application
import android.content.pm.ApplicationInfo
import com.kturker.multiplatform.core.auth.CurrentActivity
import com.kturker.multiplatform.di.initKoin
import org.koin.android.ext.koin.androidContext

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // cmpose:backend
        // Social sign-in shows its UI over the resumed Activity, so tracking starts before anything can ask
        CurrentActivity.install(this)
        // /cmpose:backend
        initKoin(isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            androidContext(androidContext = this@App)
        }
    }
}
