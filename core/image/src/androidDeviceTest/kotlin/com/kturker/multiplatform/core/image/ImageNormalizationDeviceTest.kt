package com.kturker.multiplatform.core.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The real Android decoder and encoder: what the phone does with a picked photo before uploading it. */
class ImageNormalizationDeviceTest {

    private fun encoded(width: Int, height: Int, format: Bitmap.CompressFormat, alpha: Boolean = false): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Noise-like content, so the encoders cannot shrink it to nothing
        val pixels = IntArray(width) { x -> Color.argb(if (alpha) x % 256 else 255, x % 251, x * 7 % 253, x * 13 % 255) }
        for (y in 0 until height) {
            for (x in 0 until width) pixels[x] = pixels[x] xor (y * 31)
            bitmap.setPixels(pixels, 0, width, 0, y, width, 1)
        }
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(format, 100, out)
            bitmap.recycle()
            out.toByteArray()
        }
    }

    private fun assertUploadable(bytes: ByteArray?, expectedWidth: Int, expectedHeight: Int) {
        assertNotNull(bytes)
        assertEquals(0xFF.toByte(), bytes[0])
        assertEquals(0xD8.toByte(), bytes[1])
        assertTrue(bytes.size <= MAX_IMAGE_BYTES, "size ${bytes.size}")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        assertEquals(expectedWidth, bounds.outWidth)
        assertEquals(expectedHeight, bounds.outHeight)
        assertTrue(maxOf(bounds.outWidth, bounds.outHeight) <= MAX_IMAGE_SIDE)
    }

    @Test
    fun aLargePngBecomesAJpegWithin2048PxAnd5Mb() = runTest {
        val png = encoded(4000, 3000, Bitmap.CompressFormat.PNG, alpha = true)
        assertTrue(png.size > MAX_IMAGE_BYTES, "the input should be over the limit, was ${png.size}")

        assertUploadable(normalizeImageForUpload(png), 2048, 1536)
    }

    @Test
    fun aSmallJpegKeepsItsSize() = runTest {
        assertUploadable(normalizeImageForUpload(encoded(640, 480, Bitmap.CompressFormat.JPEG)), 640, 480)
    }

    @Test
    fun aPortraitWebpIsScaledByItsHeight() = runTest {
        @Suppress("DEPRECATION")
        val webp = encoded(1500, 3000, Bitmap.CompressFormat.WEBP)

        assertUploadable(normalizeImageForUpload(webp), 1024, 2048)
    }

    @Test
    fun bytesThatAreNotAnImageGiveNull() = runTest {
        assertNull(normalizeImageForUpload("not an image".encodeToByteArray()))
        assertNull(normalizeImageForUpload(ByteArray(0)))
    }
}
