package com.kturker.multiplatform.core.navigation

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DeepLinkHandlerTest {

    private data object Preferences : NavigationCommand.Destination

    private object SettingsRoute : DeepLinkRoute {
        override val pattern = "settings"

        override fun destination(arguments: Map<String, String>) = Preferences
    }

    private lateinit var navigationManager: NavigationManagerImpl
    private lateinit var handler: DeepLinkHandler

    // NavigationManagerImpl reads Dispatchers.Main when it is created: build it once the test dispatcher is set
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        navigationManager = NavigationManagerImpl()
        handler = DeepLinkHandler(navigationManager = navigationManager, routes = { listOf(SettingsRoute) })
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // A cold start: the link arrives before the NavHost listens, the screen opens once it does
    @Test
    fun aLinkHandledBeforeTheScreenListensIsDeliveredOnceItListens() = runTest {
        assertTrue(handler.handle("multiplatform://settings"))

        navigationManager.navigationCommandFlow.test {
            assertEquals(Preferences, awaitItem())
        }
    }

    // The scheme alone just opens the app at its start screen
    @Test
    fun anUnknownLinkNavigatesNowhere() = runTest {
        assertFalse(handler.handle("multiplatform://nowhere"))
        assertFalse(handler.handle("otherapp://settings"))
        assertTrue(handler.handle("multiplatform://"))

        navigationManager.navigationCommandFlow.test {
            expectNoEvents()
        }
    }
}
