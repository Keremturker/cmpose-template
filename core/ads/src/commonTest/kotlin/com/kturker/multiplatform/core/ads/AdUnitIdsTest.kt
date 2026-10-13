package com.kturker.multiplatform.core.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdUnitIdsTest {

    @Test
    fun everyUnitIsAGoogleTestUnit() {
        listOf(AdUnitIds.banner, AdUnitIds.interstitial, AdUnitIds.rewarded).forEach {
            assertTrue(it.startsWith("ca-app-pub-3940256099942544/"), it)
        }
    }

    @Test
    fun unitsAreDistinct() {
        assertEquals(3, setOf(AdUnitIds.banner, AdUnitIds.interstitial, AdUnitIds.rewarded).size)
    }
}
