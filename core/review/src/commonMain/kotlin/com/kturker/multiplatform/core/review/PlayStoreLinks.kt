package com.kturker.multiplatform.core.review

internal data class PlayStoreLinks(val app: String, val web: String)

// The Play Store app first; a device without it opens the web page
internal fun playStoreLinks(packageName: String): PlayStoreLinks = PlayStoreLinks(
    app = "market://details?id=$packageName",
    web = "https://play.google.com/store/apps/details?id=$packageName"
)
