package com.kturker.multiplatform.core.image

import coil3.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual fun imageCacheDirectory(context: PlatformContext): Path {
    val caches = NSFileManager.defaultManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask).first() as NSURL
    return "${requireNotNull(caches.path)}/$DISK_CACHE_DIRECTORY".toPath()
}
