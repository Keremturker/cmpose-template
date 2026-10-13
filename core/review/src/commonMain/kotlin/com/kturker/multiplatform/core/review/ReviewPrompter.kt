package com.kturker.multiplatform.core.review

import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Single

private const val FIRST_THRESHOLD = 3
private const val REPEAT_INTERVAL = 10
private const val KEY_POSITIVE_EVENTS = "review.positiveEvents"
private const val KEY_NEXT_THRESHOLD = "review.nextThreshold"

private val log = Logger.withTag("Review")

/**
 * Asks for a review after the user has done something they wanted: call [recordPositiveEvent] right
 * after such a moment (a task finished, an item saved), never at launch or in the middle of a task.
 * The third positive event asks, then every tenth. No "Do you like the app?" question first and no
 * rating screen of your own: Apple and Google allow only the system dialog. ReviewRequestEffect (in
 * MainScreen) passes each request on to the platform.
 */
@Single
class ReviewPrompter(private val store: ReviewCounterStore) {

    private val pending = Channel<ReviewRequest>(capacity = Channel.BUFFERED)

    val requests: Flow<ReviewRequest> = pending.receiveAsFlow()

    fun recordPositiveEvent() {
        val count = store.getInt(key = KEY_POSITIVE_EVENTS, default = 0) + 1
        store.putInt(key = KEY_POSITIVE_EVENTS, value = count)
        val threshold = store.getInt(key = KEY_NEXT_THRESHOLD, default = FIRST_THRESHOLD)
        if (shouldRequest(count = count, threshold = threshold)) {
            store.putInt(key = KEY_NEXT_THRESHOLD, value = count + REPEAT_INTERVAL)
            log.d { "Review requested" }
            pending.trySend(ReviewRequest.Review)
        }
    }

    // The settings screen's "Rate the app"
    fun openStorePage() {
        pending.trySend(ReviewRequest.StorePage)
    }
}

internal fun shouldRequest(
    count: Int,
    threshold: Int
): Boolean = count >= threshold
