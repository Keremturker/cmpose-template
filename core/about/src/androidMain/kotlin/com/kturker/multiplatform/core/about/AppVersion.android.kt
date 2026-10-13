package com.kturker.multiplatform.core.about

import android.content.Context
import org.koin.mp.KoinPlatform.getKoin

actual fun currentAppVersion(): AppVersion {
    val context: Context = getKoin().get()
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    return AppVersion(name = info.versionName.orEmpty(), code = info.longVersionCode.toString())
}
