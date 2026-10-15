package com.kturker.multiplatform.server

import com.kturker.multiplatform.server.files.FileStore
import com.kturker.multiplatform.server.files.ImageSniffer
import java.io.IOException
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FileStoreTest {
    @Test
    fun `images are recognised by their bytes, not their names`() {
        assertEquals("image/jpeg", ImageSniffer.mimeOf(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0x00)))
        assertEquals("image/png", ImageSniffer.mimeOf(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)))
        assertEquals("image/webp", ImageSniffer.mimeOf("RIFF\u0000\u0000\u0000\u0000WEBPVP8 ".toByteArray(Charsets.ISO_8859_1)))
        assertNull(ImageSniffer.mimeOf("%PDF-1.7".toByteArray()))
        assertNull(ImageSniffer.mimeOf("<html>".toByteArray()))
        assertNull(ImageSniffer.mimeOf(ByteArray(0)))
    }

    @Test
    fun `paths outside the root are neither read, deleted nor written`() {
        val parent = createTempDirectory("store").toFile()
        val outside = parent.resolve("secret").apply { writeText("x") }
        val store = FileStore(parent.resolve("root").toPath())
        val path = store.write("s1", "f1", byteArrayOf(1))
        assertNotNull(store.read(path))
        assertNull(store.read("../secret"))
        assertNull(store.read("s1/../../secret"))
        assertNull(store.read(outside.absolutePath))
        assertNull(store.read(""))
        store.delete("../secret")
        assertTrue(outside.isFile)
        assertFailsWith<IllegalArgumentException> { store.write("..", "secret", byteArrayOf(2)) }
        assertEquals("x", outside.readText())
    }

    @Test
    fun `deleting a space folder removes it and nothing outside it`() {
        val parent = createTempDirectory("store").toFile()
        val store = FileStore(parent.resolve("root").toPath())
        store.write("s1", "f1", byteArrayOf(1))
        store.write("s2", "f2", byteArrayOf(2))
        store.deleteSpaceDir("s1")
        assertTrue(!parent.resolve("root/s1").exists())
        assertTrue(parent.resolve("root/s2/f2").isFile)
        // Not a single path segment: ignored rather than resolved
        store.deleteSpaceDir("..")
        store.deleteSpaceDir("s2/..")
        store.deleteSpaceDir("")
        assertTrue(parent.resolve("root/s2/f2").isFile)
        // A space that never stored a file
        store.deleteSpaceDir("empty")
    }

    @Test
    fun `a file or folder that cannot be removed is reported`() {
        val root = createTempDirectory("store").toFile()
        val store = FileStore(root.toPath())
        val path = store.write("s1", "f1", byteArrayOf(1))
        withReadOnlyDir(root.resolve("s1")) {
            assertFailsWith<IOException> { store.delete(path) }
            assertFailsWith<IOException> { store.deleteSpaceDir("s1") }
        }
        assertTrue(root.resolve("s1/f1").isFile)
        // Once removable, both succeed; a file that is already gone is not an error
        store.delete(path)
        store.delete(path)
        store.deleteSpaceDir("s1")
        assertTrue(!root.resolve("s1").exists())
    }
}
