package com.kturker.multiplatform.core.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MinVersionParsingTest {

    @Test
    fun bothPlatformsAreRead() {
        assertEquals(
            MinVersions(android = 12, ios = "1.4.0"),
            parseMinVersions("""{"minVersion":{"android":12,"ios":"1.4.0"}}""")
        )
    }

    @Test
    fun aMissingPlatformHasNoMinimum() {
        val minVersions = parseMinVersions("""{"minVersion":{"ios":"1.4.0"}}""")

        assertNull(minVersions?.android)
        assertEquals("1.4.0", minVersions?.ios)
    }

    // A broken file must not crash the app: there is simply no forced update
    @Test
    fun aBrokenFileIsIgnored() {
        assertNull(parseMinVersions("""{"minVersion":"""))
        assertNull(parseMinVersions("<html>Not found</html>"))
    }
}
