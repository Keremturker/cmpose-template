package com.kturker.multiplatform.core.image

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryGetValue
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFNumberCreate
import platform.CoreFoundation.CFNumberGetValue
import platform.CoreFoundation.CFNumberRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFNumberIntType
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.create
import platform.ImageIO.CGImageSourceCopyPropertiesAtIndex
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.CGImageSourceRef
import platform.ImageIO.kCGImagePropertyPixelHeight
import platform.ImageIO.kCGImagePropertyPixelWidth
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceCreateThumbnailWithTransform
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIRectFill
import platform.posix.memcpy

// ImageIO reads HEIC as well and decodes straight to the target size, so a 48 MP photo is never held in full;
// the thumbnail options also apply the EXIF orientation
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun normalizeImageForUpload(bytes: ByteArray): ByteArray? =
    withContext(Dispatchers.Default) {
        if (bytes.isEmpty()) return@withContext null
        val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
        val cgImage = decodeScaled(data) ?: return@withContext null
        try {
            val image = if (cgImage.hasAlpha()) flattenOnWhite(cgImage) else UIImage.imageWithCGImage(cgImage)
            encodeWithinLimit { quality ->
                autoreleasepool {
                    UIImageJPEGRepresentation(image, quality.toDouble() / MAX_JPEG_QUALITY)?.toByteArray()
                }
            }
        } finally {
            CGImageRelease(cgImage)
        }
    }

@OptIn(ExperimentalForeignApi::class)
private fun decodeScaled(data: NSData): CGImageRef? {
    val cfData: CFDataRef = CFBridgingRetain(data)?.reinterpret() ?: return null
    val source = CGImageSourceCreateWithData(cfData, null)
    CFRelease(cfData)
    if (source == null) return null
    return try {
        thumbnail(source)
    } finally {
        CFRelease(source)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun thumbnail(source: CGImageSourceRef): CGImageRef? {
    val properties = CGImageSourceCopyPropertiesAtIndex(source, 0u, null) ?: return null
    val width = properties.intValue(kCGImagePropertyPixelWidth)
    val height = properties.intValue(kCGImagePropertyPixelHeight)
    CFRelease(properties)
    if (width == null || height == null || width < 1 || height < 1) return null
    val target = targetSize(width, height)
    val options = thumbnailOptions(maxPixelSize = maxOf(target.width, target.height))
    return try {
        CGImageSourceCreateThumbnailAtIndex(source, 0u, options)
    } finally {
        CFRelease(options)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun thumbnailOptions(maxPixelSize: Int): CFDictionaryRef {
    val options = checkNotNull(
        CFDictionaryCreateMutable(null, 3, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr)
    )
    CFDictionarySetValue(options, kCGImageSourceCreateThumbnailFromImageAlways, kCFBooleanTrue)
    CFDictionarySetValue(options, kCGImageSourceCreateThumbnailWithTransform, kCFBooleanTrue)
    memScoped {
        val size = alloc<IntVar>().apply { value = maxPixelSize }
        val number = CFNumberCreate(null, kCFNumberIntType, size.ptr)
        CFDictionarySetValue(options, kCGImageSourceThumbnailMaxPixelSize, number)
        CFRelease(number)
    }
    return options
}

@OptIn(ExperimentalForeignApi::class)
private fun CFDictionaryRef.intValue(key: CFStringRef?): Int? {
    val number: CFNumberRef = CFDictionaryGetValue(this, key)?.reinterpret() ?: return null
    return memScoped {
        val out = alloc<IntVar>()
        if (CFNumberGetValue(number, kCFNumberIntType, out.ptr)) out.value else null
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CGImageRef.hasAlpha(): Boolean = when (CGImageGetAlphaInfo(this)) {
    CGImageAlphaInfo.kCGImageAlphaNone,
    CGImageAlphaInfo.kCGImageAlphaNoneSkipFirst,
    CGImageAlphaInfo.kCGImageAlphaNoneSkipLast -> false

    else -> true
}

// JPEG has no alpha: transparent pixels would turn black, so they go onto white first (at the scaled size)
@OptIn(ExperimentalForeignApi::class)
private fun flattenOnWhite(cgImage: CGImageRef): UIImage {
    val width = CGImageGetWidth(cgImage).toDouble()
    val height = CGImageGetHeight(cgImage).toDouble()
    val format = UIGraphicsImageRendererFormat.defaultFormat().apply {
        scale = 1.0
        opaque = true
    }
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(width, height), format = format)
    val rect = CGRectMake(0.0, 0.0, width, height)
    val source = UIImage.imageWithCGImage(cgImage)
    return renderer.imageWithActions { _ ->
        UIColor.whiteColor.setFill()
        UIRectFill(rect)
        source.drawInRect(rect)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val out = ByteArray(size)
    if (size > 0) out.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return out
}
