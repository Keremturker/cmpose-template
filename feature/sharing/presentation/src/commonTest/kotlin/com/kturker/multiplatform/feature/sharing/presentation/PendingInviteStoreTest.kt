package com.kturker.multiplatform.feature.sharing.presentation

import com.kturker.multiplatform.feature.sharing.presentation.data.DataStorePendingInviteStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PendingInviteStoreTest {

    private val dataStore = InMemoryPreferences()

    @Test
    fun takeReturnsTheTokenOnce() = runTest {
        val store = DataStorePendingInviteStore(dataStore)

        store.put("abc")

        assertEquals("abc", store.take())
        assertNull(store.take())
    }

    @Test
    fun nothingPutTakesNothing() = runTest {
        assertNull(DataStorePendingInviteStore(dataStore).take())
    }

    // The latest link the user opened is the one they meant
    @Test
    fun aNewerInviteReplacesTheOlderOne() = runTest {
        val store = DataStorePendingInviteStore(dataStore)

        store.put("old")
        store.put("new")

        assertEquals("new", store.take())
        assertNull(store.take())
    }

    // Stored, not held in memory: the app may be restarted between opening the link and signing in
    @Test
    fun theTokenOutlivesTheStoreInstance() = runTest {
        DataStorePendingInviteStore(dataStore).put("abc")

        assertEquals("abc", DataStorePendingInviteStore(dataStore).take())
    }
}
