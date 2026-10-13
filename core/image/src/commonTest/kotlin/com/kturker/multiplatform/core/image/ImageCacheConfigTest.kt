package com.kturker.multiplatform.core.image

import kotlin.test.Test
import kotlin.test.assertEquals

class ImageCacheConfigTest {

    @Test
    fun theMemoryCacheTakesAQuarterOfTheAppsMemory() {
        assertEquals(0.25, MEMORY_CACHE_PERCENT)
    }

    // A fixed size: Coil would ask for the free disk space to size it by percent
    @Test
    fun theDiskCacheIsCappedAt50MbInItsOwnDirectory() {
        assertEquals(50L * 1024 * 1024, DISK_CACHE_MAX_BYTES)
        assertEquals("image_cache", DISK_CACHE_DIRECTORY)
    }
}
