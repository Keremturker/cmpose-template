package com.kturker.multiplatform.core.review

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

private val log = Logger.withTag("Review")

@Composable
actual fun rememberReviewRequester(): ReviewRequester {
    val activity = requireNotNull(LocalActivity.current) { "In-app review needs the hosting Activity" }
    return remember(activity) { AndroidReviewRequester(activity) }
}

// Play shows the dialog only to an app installed from Google Play (an internal test track works) and
// limits how often; on an emulator or a sideloaded build the flow completes without a dialog.
internal class AndroidReviewRequester(private val activity: Activity) : ReviewRequester {

    private val manager: ReviewManager = ReviewManagerFactory.create(activity)

    override suspend fun requestReview() {
        try {
            val reviewInfo = manager.requestReview()
            manager.launchReview(activity, reviewInfo)
        } catch (e: ReviewException) {
            log.w { "Review flow unavailable: error ${e.errorCode}" }
        }
    }

    // The application ID is the package name, so the generated project's own ID is used
    override fun openStorePage() {
        val links = playStoreLinks(packageName = activity.packageName)
        if (!activity.open(links.app) && !activity.open(links.web)) {
            log.w { "No app can open the Play Store page" }
        }
    }
}

private fun Activity.open(uri: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    true
} catch (_: ActivityNotFoundException) {
    false
}
