package com.kturker.multiplatform.core.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import platform.Foundation.NSURL
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindowScene

private val log = Logger.withTag("Review")

@Composable
actual fun rememberReviewRequester(): ReviewRequester = remember { IosReviewRequester() }

// Debug builds show the dialog every time; TestFlight builds never do; App Store builds a few times a
// year at most.
internal class IosReviewRequester : ReviewRequester {

    override suspend fun requestReview() {
        showSystemReviewDialog()
    }

    override fun openStorePage() {
        val url = appStoreWriteReviewUrl(appStoreId = APP_STORE_ID)?.let { NSURL.URLWithString(it) }
        if (url == null) {
            showSystemReviewDialog()
            return
        }
        UIApplication.sharedApplication.openURL(
            url,
            options = emptyMap<Any?, Any?>(),
            completionHandler = { opened -> if (!opened) log.w { "Could not open the App Store page" } }
        )
    }
}

// StoreKit 2's AppStore.requestReview(in:) is Swift-only, so Kotlin calls the Objective-C API it
// replaced: deprecated in iOS 18, still supported and shown by the system.
@Suppress("DEPRECATION")
private fun showSystemReviewDialog() {
    val scene = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
    if (scene == null) {
        log.w { "No active window scene: review not requested" }
        return
    }
    SKStoreReviewController.requestReviewInScene(scene)
}
