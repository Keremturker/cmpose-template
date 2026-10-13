package com.kturker.multiplatform.core.image

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath

internal actual fun imageCacheDirectory(context: PlatformContext): Path =
    context.cacheDir.resolve(DISK_CACHE_DIRECTORY).absolutePath.toPath()
