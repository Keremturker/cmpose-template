package com.kturker.multiplatform.core.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StoreLinksTest {

    @Test
    fun thePlayStorePageOpensInThePlayStoreAppOrTheBrowser() {
        val links = playStoreLinks(packageName = "com.example.app")

        assertEquals("market://details?id=com.example.app", links.app)
        assertEquals("https://play.google.com/store/apps/details?id=com.example.app", links.web)
    }

    // Until APP_STORE_ID is filled in, iOS shows the system's review dialog instead
    @Test
    fun anEmptyAppStoreIdHasNoPage() {
        assertNull(appStoreWriteReviewUrl(appStoreId = ""))
        assertNull(appStoreWriteReviewUrl(appStoreId = "  "))
    }

    @Test
    fun anAppStoreIdOpensItsWriteAReviewPage() {
        assertEquals(
            "https://apps.apple.com/app/id1234567890?action=write-review",
            appStoreWriteReviewUrl(appStoreId = "1234567890")
        )
    }
}
