package com.kturker.multiplatform

import android.app.Application
import android.content.pm.ApplicationInfo
import com.kturker.multiplatform.di.initKoin
import org.koin.android.ext.koin.androidContext

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin(isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            androidContext(androidContext = this@App)
        }
    }
}
