package com.kturker.multiplatform.core.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersionComparisonTest {

    @Test
    fun aHigherPatchIsNewer() {
        assertTrue(compareVersionNames("1.4.1", "1.4.0") > 0)
        assertTrue(compareVersionNames("1.4.0", "1.4.1") < 0)
    }

    @Test
    fun aMissingPartCountsAsZero() {
        assertEquals(0, compareVersionNames("1.4", "1.4.0"))
    }

    // 10 is more than 9: the parts compare as numbers, not as text
    @Test
    fun partsCompareAsNumbers() {
        assertTrue(compareVersionNames("1.10.0", "1.9.9") > 0)
    }

    @Test
    fun aSuffixIsIgnored() {
        assertEquals(0, compareVersionNames("2.0.0-beta", "2.0.0"))
    }

    @Test
    fun anEmptyVersionIsTheOldest() {
        assertTrue(compareVersionNames("", "0.0.1") < 0)
        assertTrue(compareVersionNames("1.0.0", "") > 0)
    }
}
