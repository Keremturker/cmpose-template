package com.kturker.multiplatform.core.ads

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Back moves the screen out of RESUMED at once, while it stays composed for the exit transition.
@OptIn(ExperimentalCoroutinesApi::class)
class PresentWhenResumedTest {

    private val screen = TestScreen()
    private var presented: CompletableDeferred<String>? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.present() =
        async { screen.lifecycle.presentWhenResumed<String> { presented = it } }.also { runCurrent() }

    @Test
    fun aResumedScreenPresentsAtOnce() = runTest {
        screen.moveTo(Lifecycle.State.RESUMED)

        val result = present()
        presented?.complete("dismissed")

        assertEquals("dismissed", result.await())
    }

    @Test
    fun aScreenThatWasLeftDoesNotPresent() = runTest {
        screen.moveTo(Lifecycle.State.RESUMED)
        screen.moveTo(Lifecycle.State.CREATED)

        val result = present()
        assertNull(presented)
        // The exit transition ends: the screen's call is cancelled
        result.cancel()
        runCurrent()

        assertNull(presented)
        assertTrue(result.isCancelled)
    }

    @Test
    fun aDestroyedScreenDoesNotPresent() = runTest {
        screen.moveTo(Lifecycle.State.RESUMED)
        screen.moveTo(Lifecycle.State.CREATED)
        val result = present()

        screen.moveTo(Lifecycle.State.DESTROYED)
        runCurrent()

        assertNull(presented)
        assertTrue(result.isCompleted)
    }

    @Test
    fun aScreenThatResumesAgainPresentsThen() = runTest {
        screen.moveTo(Lifecycle.State.STARTED)

        val result = present()
        assertNull(presented)
        screen.moveTo(Lifecycle.State.RESUMED)
        runCurrent()
        presented?.complete("dismissed")

        assertEquals("dismissed", result.await())
    }

    // The ad on screen pauses (Android) or stops (iOS) its own screen: that must not drop the result.
    @Test
    fun anAdOnScreenKeepsItsResultWhenTheScreenStops() = runTest {
        screen.moveTo(Lifecycle.State.RESUMED)

        val result = present()
        screen.moveTo(Lifecycle.State.CREATED)
        runCurrent()
        presented?.complete("rewarded")

        assertEquals("rewarded", result.await())
    }

    // Android destroys the screen's lifecycle on rotation while the ad stays open: the result still arrives
    // (the caller runs in the ViewModel's scope, which survives the recreation).
    @Test
    fun anAdOnScreenKeepsItsResultWhenTheScreenIsDestroyed() = runTest {
        screen.moveTo(Lifecycle.State.RESUMED)

        val result = present()
        screen.moveTo(Lifecycle.State.DESTROYED)
        runCurrent()
        presented?.complete("rewarded")

        assertEquals("rewarded", result.await())
    }
}

// A navigation entry's lifecycle, moved by hand.
internal class TestScreen : LifecycleOwner {
    private val registry = LifecycleRegistry.createUnsafe(this)
    override val lifecycle: Lifecycle get() = registry

    fun moveTo(state: Lifecycle.State) {
        registry.currentState = state
    }
}
