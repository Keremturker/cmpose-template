package com.kturker.multiplatform.core.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject

// Call once in MainScreen: it hands ReviewPrompter's requests to the platform (Android needs the
// Activity, which only the UI has)
@Composable
fun ReviewRequestEffect(prompter: ReviewPrompter = koinInject()) {
    val requester = rememberReviewRequester()
    LaunchedEffect(prompter, requester) {
        prompter.requests.collect { request ->
            when (request) {
                ReviewRequest.Review -> requester.requestReview()
                ReviewRequest.StorePage -> requester.openStorePage()
            }
        }
    }
}
