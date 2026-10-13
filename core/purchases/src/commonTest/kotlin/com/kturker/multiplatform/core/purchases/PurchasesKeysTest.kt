package com.kturker.multiplatform.core.purchases

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// commonTest runs on both platforms, so the key checks cover the androidMain and the iosMain key.
class PurchasesKeysTest {

    @Test
    fun thePlatformKeyIsSet() {
        assertTrue(revenueCatApiKey.isNotBlank())
    }

    @Test
    fun theShippedKeyIsATestStoreKey() {
        assertTrue(PurchasesKeys.isTestStoreKey(revenueCatApiKey))
    }

    @Test
    fun platformKeysAreNotTestStoreKeys() {
        assertFalse(PurchasesKeys.isTestStoreKey("appl_abc"))
        assertFalse(PurchasesKeys.isTestStoreKey("goog_abc"))
    }

    @Test
    fun debugBuildsConfigureWithAnyKey() {
        assertTrue(PurchasesKeys.canConfigure(apiKey = revenueCatApiKey, isDebug = true))
        assertTrue(PurchasesKeys.canConfigure(apiKey = "goog_abc", isDebug = true))
    }

    @Test
    fun releaseBuildsSkipATestStoreKey() {
        assertFalse(PurchasesKeys.canConfigure(apiKey = revenueCatApiKey, isDebug = false))
        assertTrue(PurchasesKeys.canConfigure(apiKey = "appl_abc", isDebug = false))
    }

    @Test
    fun premiumNeedsThePremiumEntitlement() {
        assertTrue(hasPremium(activeEntitlementIds = setOf("premium", "other")))
        assertFalse(hasPremium(activeEntitlementIds = setOf("other")))
    }

    @Test
    fun premiumIsUnknownUntilRevenueCatAnswers() {
        assertNull(initialPremium(isAvailable = true))
    }

    @Test
    fun withoutRevenueCatNobodyIsPremium() {
        // A release build with a Test Store key: ads must still run, not wait for an answer that never comes.
        assertEquals(false, initialPremium(isAvailable = false))
    }
}
