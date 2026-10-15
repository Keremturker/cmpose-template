package com.kturker.multiplatform.server.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

/** A file row: [path] is relative to the file store's root; [mime] is the type sniffed from the bytes. */
data class StoredFile(val id: UUID, val spaceId: UUID, val path: String, val mime: String, val size: Long)

/** Files that belong to a space. Callers check the caller's role in [StoredFile.spaceId] first (`requireRole`). */
class FileRepository(private val db: Database) {
    fun add(
        file: StoredFile,
        uploadedBy: String
    ) {
        transaction(db) {
            Files.insert {
                it[id] = file.id
                it[spaceId] = file.spaceId
                it[Files.uploadedBy] = uploadedBy
                it[path] = file.path
                it[mime] = file.mime
                it[size] = file.size
                it[createdAt] = System.currentTimeMillis()
            }
        }
    }

    fun get(id: UUID): StoredFile? = transaction(db) {
        Files.selectAll().where { Files.id eq id }.singleOrNull()?.toStoredFile()
    }

    /** False when the row was already gone. */
    fun delete(id: UUID): Boolean = transaction(db) { Files.deleteWhere { Files.id eq id } > 0 }

    private fun ResultRow.toStoredFile() = StoredFile(
        id = this[Files.id],
        spaceId = this[Files.spaceId],
        path = this[Files.path],
        mime = this[Files.mime],
        size = this[Files.size]
    )
}
