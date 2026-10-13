package com.kturker.multiplatform.core.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppStoreLookupTest {

    @Test
    fun theFirstResultIsTheStoreRelease() {
        val json = """{"resultCount":1,"results":[""" +
            """{"version":"2.0.1","trackViewUrl":"https://apps.apple.com/app/id1"}]}"""

        assertEquals(
            StoreRelease(version = "2.0.1", storeUrl = "https://apps.apple.com/app/id1"),
            parseStoreRelease(json)
        )
    }

    // The app is not on the App Store (yet)
    @Test
    fun noResultHasNoStoreRelease() {
        assertNull(parseStoreRelease("""{"resultCount":0,"results":[]}"""))
    }
}
