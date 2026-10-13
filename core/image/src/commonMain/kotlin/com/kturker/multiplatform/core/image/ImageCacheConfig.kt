package com.kturker.multiplatform.core.image

import coil3.PlatformContext
import okio.Path

internal const val MEMORY_CACHE_PERCENT = 0.25
internal const val DISK_CACHE_MAX_BYTES = 50L * 1024 * 1024
internal const val DISK_CACHE_DIRECTORY = "image_cache"

// The platform's cache directory (Android Context.cacheDir, iOS NSCachesDirectory) + DISK_CACHE_DIRECTORY
internal expect fun imageCacheDirectory(context: PlatformContext): Path
