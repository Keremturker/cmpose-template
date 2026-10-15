package com.kturker.multiplatform.core.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer

actual suspend fun normalizeImageForUpload(bytes: ByteArray): ByteArray? =
    withContext(Dispatchers.Default) {
        val decoded = decodeScaled(bytes) ?: return@withContext null
        // JPEG has no alpha: transparent pixels would turn black, so they go onto white first
        val bitmap = if (decoded.hasAlpha()) flattenOnWhite(decoded) else decoded
        try {
            encodeWithinLimit { quality ->
                ByteArrayOutputStream().use { out ->
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) out.toByteArray() else null
                }
            }
        } finally {
            bitmap.recycle()
        }
    }

private fun decodeScaled(bytes: ByteArray): Bitmap? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        decodeWithImageDecoder(bytes)
    } else {
        decodeWithBitmapFactory(bytes)
    }
} catch (_: IOException) {
    null
} catch (_: IllegalArgumentException) {
    null
} catch (_: OutOfMemoryError) {
    // An image too big to decode even scaled is refused like an undecodable one
    null
}

// ImageDecoder reads HEIC/HEIF as well, applies the EXIF orientation itself and scales while decoding
private fun decodeWithImageDecoder(bytes: ByteArray): Bitmap {
    val source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
    return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val target = targetSize(info.size.width, info.size.height)
        decoder.setTargetSize(target.width, target.height)
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }
}

// Before API 28: subsample while decoding, scale to the exact size, then turn by the EXIF orientation
private fun decodeWithBitmapFactory(bytes: ByteArray): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val target = targetSize(bounds.outWidth, bounds.outHeight)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= target.width && bounds.outHeight / (sample * 2) >= target.height) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    val sampled = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null
    val matrix = Matrix().apply {
        postScale(target.width.toFloat() / sampled.width, target.height.toFloat() / sampled.height)
        postRotate(exifRotation(bytes))
    }
    val result = Bitmap.createBitmap(sampled, 0, 0, sampled.width, sampled.height, matrix, true)
    if (result !== sampled) sampled.recycle()
    return result
}

private const val QUARTER_TURN = 90f
private const val HALF_TURN = 180f
private const val THREE_QUARTER_TURN = 270f

private fun exifRotation(bytes: ByteArray): Float = try {
    when (
        ExifInterface(ByteArrayInputStream(bytes))
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> QUARTER_TURN
        ExifInterface.ORIENTATION_ROTATE_180 -> HALF_TURN
        ExifInterface.ORIENTATION_ROTATE_270 -> THREE_QUARTER_TURN
        else -> 0f
    }
} catch (_: IOException) {
    0f
}

private fun flattenOnWhite(source: Bitmap): Bitmap {
    val opaque = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    Canvas(opaque).apply {
        drawColor(Color.WHITE)
        drawBitmap(source, 0f, 0f, null)
    }
    source.recycle()
    return opaque
}
