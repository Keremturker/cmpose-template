package com.kturker.multiplatform.server.files

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

const val MAX_IMAGE_BYTES = 5 * 1024 * 1024

// Signatures as ISO-8859-1 text, so each char is exactly one byte.
private val JPEG = signature("\u00FF\u00D8\u00FF")
private val PNG = signature("\u0089PNG\r\n\u001A\n")
private val RIFF = signature("RIFF")
private val WEBP = signature("WEBP")
private const val WEBP_TAG_OFFSET = 8

private fun signature(text: String) = text.toByteArray(Charsets.ISO_8859_1)

private fun ByteArray.hasAt(
    offset: Int,
    expected: ByteArray
) = size >= offset + expected.size && expected.indices.all { this[offset + it] == expected[it] }

/** Tells the image type from the content bytes; file names and `Content-Type` headers are never trusted. */
object ImageSniffer {
    fun mimeOf(bytes: ByteArray): String? = when {
        bytes.hasAt(0, JPEG) -> "image/jpeg"
        bytes.hasAt(0, PNG) -> "image/png"
        bytes.hasAt(0, RIFF) && bytes.hasAt(WEBP_TAG_OFFSET, WEBP) -> "image/webp"
        else -> null
    }
}

/** Files live under `<root>/<spaceId>/<fileId>`; paths stored in the database are relative to [root]. */
class FileStore(root: Path) {
    private val root: Path = root.toAbsolutePath().normalize()

    /** Resolves [relative] under [root], or null when it would escape it (path traversal). */
    private fun inside(relative: String): Path? =
        root.resolve(relative).normalize().takeIf { it.startsWith(root) && it != root }

    fun write(
        spaceId: String,
        fileId: String,
        bytes: ByteArray
    ): String {
        val relative = "$spaceId/$fileId"
        val file = requireNotNull(inside(relative)) { "File path escapes the store" }.toFile()
        file.parentFile.mkdirs()
        file.writeBytes(bytes)
        return relative
    }

    fun read(path: String): File? = inside(path)?.toFile()?.takeIf { it.isFile }

    /**
     * Removes the file at [path]; a missing file, a folder or a path outside [root] is ignored.
     *
     * @throws IOException when the file exists but cannot be removed
     */
    fun delete(path: String) {
        inside(path)?.takeIf { !Files.isDirectory(it) }?.let(Files::deleteIfExists)
    }

    /**
     * Removes a space's folder (space or account deletion); anything but one path segment under [root] is ignored.
     *
     * @throws IOException when the folder, or something in it, cannot be removed
     */
    fun deleteSpaceDir(spaceId: String) {
        val dir = inside(spaceId)?.takeIf { it.parent == root }?.toFile() ?: return
        if (!dir.deleteRecursively()) throw IOException("A space folder could not be removed completely")
    }
}
