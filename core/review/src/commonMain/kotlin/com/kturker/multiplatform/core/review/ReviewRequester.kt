package com.kturker.multiplatform.core.review

import androidx.compose.runtime.Composable

// The platform's own review dialog: Play In-App Review on Android, StoreKit on iOS. The system decides
// whether the dialog shows (it limits how often), and the app never learns whether the user rated it.
interface ReviewRequester {
    suspend fun requestReview()

    // The app's store page: Google Play on Android, the App Store's write-a-review page on iOS
    // (APP_STORE_ID; until it is set, the system's review dialog)
    fun openStorePage()
}

@Composable
expect fun rememberReviewRequester(): ReviewRequester
