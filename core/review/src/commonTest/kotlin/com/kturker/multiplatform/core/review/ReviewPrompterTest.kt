package com.kturker.multiplatform.core.review

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewPrompterTest {

    private val store = InMemoryReviewCounterStore()

    @Test
    fun noReviewIsAskedBeforeTheThreshold() = runTest {
        val prompter = ReviewPrompter(store = store)

        prompter.requests.test {
            repeat(times = 2) { prompter.recordPositiveEvent() }
            expectNoEvents()
        }
    }

    @Test
    fun theThirdPositiveEventAsksForAReview() = runTest {
        val prompter = ReviewPrompter(store = store)

        prompter.requests.test {
            repeat(times = 3) { prompter.recordPositiveEvent() }
            assertEquals(ReviewRequest.Review, awaitItem())
            expectNoEvents()
        }
    }

    // The system shows the dialog a few times a year at most, so the app asks again only much later
    @Test
    fun theNextReviewIsAskedTenEventsLater() = runTest {
        val prompter = ReviewPrompter(store = store)

        prompter.requests.test {
            repeat(times = 3) { prompter.recordPositiveEvent() }
            awaitItem()
            repeat(times = 9) { prompter.recordPositiveEvent() }
            expectNoEvents()
            prompter.recordPositiveEvent()
            assertEquals(ReviewRequest.Review, awaitItem())
        }
    }

    // The count is kept on the device: a restart (a new prompter on the same store) continues it
    @Test
    fun theCountSurvivesARestart() = runTest {
        repeat(times = 2) { ReviewPrompter(store = store).recordPositiveEvent() }
        val prompter = ReviewPrompter(store = store)

        prompter.requests.test {
            prompter.recordPositiveEvent()
            assertEquals(ReviewRequest.Review, awaitItem())
        }
    }

    @Test
    fun theStorePageIsPassedOn() = runTest {
        val prompter = ReviewPrompter(store = store)

        prompter.requests.test {
            prompter.openStorePage()
            assertEquals(ReviewRequest.StorePage, awaitItem())
        }
    }

    @Test
    fun aReviewIsDueFromTheThresholdOn() {
        assertFalse(shouldRequest(count = 2, threshold = 3))
        assertTrue(shouldRequest(count = 3, threshold = 3))
        assertTrue(shouldRequest(count = 4, threshold = 3))
    }
}

private class InMemoryReviewCounterStore : ReviewCounterStore {
    private val values = mutableMapOf<String, Int>()

    override fun getInt(
        key: String,
        default: Int
    ): Int = values[key] ?: default

    override fun putInt(
        key: String,
        value: Int
    ) {
        values[key] = value
    }
}
