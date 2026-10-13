package com.kturker.multiplatform.core.update

import com.kturker.multiplatform.core.about.AppVersion
import kotlin.test.Test
import kotlin.test.assertEquals

private const val STORE_URL = "https://apps.apple.com/app/id1"

class UpdateDecisionTest {

    @Test
    fun anIosVersionBelowTheMinimumIsRequired() {
        val decision = decideUpdate(
            current = AppVersion(name = "1.3.0", code = "7"),
            minVersions = MinVersions(ios = "1.4.0"),
            storeRelease = StoreRelease(version = "1.4.0", storeUrl = STORE_URL),
            platform = UpdatePlatform.Ios
        )

        assertEquals(UpdateDecision.Required(storeUrl = STORE_URL), decision)
    }

    @Test
    fun aNewerIosVersionOnTheAppStoreIsAvailable() {
        val decision = decideUpdate(
            current = AppVersion(name = "1.4.0", code = "8"),
            minVersions = MinVersions(ios = "1.4.0"),
            storeRelease = StoreRelease(version = "1.5.0", storeUrl = STORE_URL),
            platform = UpdatePlatform.Ios
        )

        assertEquals(UpdateDecision.Available(storeUrl = STORE_URL), decision)
    }

    // Android compares the versionCode; "Update" opens the Play Store page itself, so there is no URL
    @Test
    fun anAndroidVersionCodeBelowTheMinimumIsRequired() {
        val decision = decideUpdate(
            current = AppVersion(name = "1.3.0", code = "11"),
            minVersions = MinVersions(android = 12),
            storeRelease = null,
            platform = UpdatePlatform.Android
        )

        assertEquals(UpdateDecision.Required(storeUrl = null), decision)
    }

    @Test
    fun nothingKnownIsNoUpdate() {
        val decision = decideUpdate(
            current = AppVersion(name = "1.3.0", code = "11"),
            minVersions = null,
            storeRelease = null,
            platform = UpdatePlatform.Ios
        )

        assertEquals(UpdateDecision.None, decision)
    }
}
