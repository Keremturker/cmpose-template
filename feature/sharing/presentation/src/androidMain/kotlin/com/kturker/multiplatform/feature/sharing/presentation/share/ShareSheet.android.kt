package com.kturker.multiplatform.feature.sharing.presentation.share

import android.content.Context
import android.content.Intent
import com.kturker.multiplatform.core.auth.CurrentActivity
import org.koin.mp.KoinPlatform.getKoin

internal actual fun presentShareSheet(text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(send, null)
    // The chooser opens over the app's own Activity; without one (it is being recreated) as a new task
    val activity = CurrentActivity.activity
    if (activity != null) {
        activity.startActivity(chooser)
    } else {
        val context: Context = getKoin().get()
        context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

internal actual fun appDisplayName(): String {
    val context: Context = getKoin().get()
    return context.applicationInfo.loadLabel(context.packageManager).toString()
}
