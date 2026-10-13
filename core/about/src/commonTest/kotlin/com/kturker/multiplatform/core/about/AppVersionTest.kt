package com.kturker.multiplatform.core.about

import kotlin.test.Test
import kotlin.test.assertEquals

class AppVersionTest {

    @Test
    fun theVersionShowsItsNameAndBuildNumber() {
        assertEquals("1.4.0 (12)", AppVersion(name = "1.4.0", code = "12").displayText)
    }

    // A build without a build number (an unusual Info.plist) still shows its version
    @Test
    fun aMissingBuildNumberIsLeftOut() {
        assertEquals("1.4.0", AppVersion(name = "1.4.0", code = "").displayText)
    }
}
