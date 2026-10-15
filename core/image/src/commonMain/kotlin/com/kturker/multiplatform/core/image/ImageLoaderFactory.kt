package com.kturker.multiplatform.core.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade

// The app's one ImageLoader. Network images need core:network (coil-network-ktor3 registers itself);
// without it, local images (Res, files) still load. A component from [customizers] comes before the fetcher
// coil-network-ktor3 registers on its own.
fun newImageLoader(
    context: PlatformContext,
    isDebug: Boolean,
    customizers: List<ImageLoaderCustomizer> = emptyList()
): ImageLoader = ImageLoader.Builder(context)
    .components { customizers.forEach { with(it) { customize() } } }
    .memoryCache { MemoryCache.Builder().maxSizePercent(context, MEMORY_CACHE_PERCENT).build() }
    .diskCache {
        DiskCache.Builder()
            .directory(imageCacheDirectory(context))
            .maxSizeBytes(DISK_CACHE_MAX_BYTES)
            .build()
    }
    .crossfade(true)
    .logger(imageLogger(isDebug))
    .build()
