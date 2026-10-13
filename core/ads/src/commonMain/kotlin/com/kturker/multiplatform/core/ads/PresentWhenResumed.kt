package com.kturker.multiplatform.core.ads

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.withStateAtLeast
import kotlinx.coroutines.CompletableDeferred

// A loaded ad (or consent form) opens only while its screen (the navigation entry) is resumed. Back moves the
// screen out of RESUMED at once, so one that loads during the exit transition waits here and never opens; it is
// dropped when the screen's lifecycle is destroyed (withStateAtLeast then throws) or its caller is cancelled
// (the ViewModel cleared). A screen that is only CREATED or STARTED (the app in the background, another screen
// on top) keeps it waiting, and it opens once the screen resumes. Only the opening is gated: the ad on screen
// pauses (Android) or stops (iOS) the screen itself, and that must not drop its result.
// present runs on the main thread and completes the result from the ad's callbacks.
internal suspend fun <T> Lifecycle.presentWhenResumed(
    present: (result: CompletableDeferred<T>) -> Unit
): T {
    val result = CompletableDeferred<T>()
    withStateAtLeast(Lifecycle.State.RESUMED) { present(result) }
    return result.await()
}
