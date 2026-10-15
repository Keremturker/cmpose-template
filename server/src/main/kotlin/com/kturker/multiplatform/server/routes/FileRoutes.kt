package com.kturker.multiplatform.server.routes

import com.kturker.multiplatform.core.api.ApiRoutes
import com.kturker.multiplatform.core.api.ErrorCode
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.UploadResult
import com.kturker.multiplatform.server.ApiException
import com.kturker.multiplatform.server.Services
import com.kturker.multiplatform.server.access.requireRole
import com.kturker.multiplatform.server.access.uuidOrNull
import com.kturker.multiplatform.server.badRequest
import com.kturker.multiplatform.server.db.StoredFile
import com.kturker.multiplatform.server.files.ImageSniffer
import com.kturker.multiplatform.server.files.MAX_IMAGE_BYTES
import com.kturker.multiplatform.server.notFound
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.http.content.LocalFileContent
import io.ktor.server.request.contentLength
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import java.io.IOException
import java.util.UUID

// Room for the multipart headers around a full-size image.
private const val MULTIPART_OVERHEAD = 64 * 1024

private fun tooLarge(): Nothing =
    throw ApiException(HttpStatusCode.PayloadTooLarge, ErrorCode.PAYLOAD_TOO_LARGE, "Image is larger than 5 MB")

/** Reads the first file part, refusing anything over [MAX_IMAGE_BYTES] before it is buffered in full. */
private suspend fun ApplicationCall.receiveImage(): ByteArray {
    val bodyLimit = (MAX_IMAGE_BYTES + MULTIPART_OVERHEAD).toLong()
    if ((request.contentLength() ?: 0) > bodyLimit) tooLarge()
    var bytes: ByteArray? = null
    try {
        receiveMultipart(formFieldLimit = bodyLimit).forEachPart { part ->
            if (part is PartData.FileItem && bytes == null) {
                val read = part.provider().readRemaining((MAX_IMAGE_BYTES + 1).toLong()).readByteArray()
                if (read.size > MAX_IMAGE_BYTES) tooLarge()
                bytes = read
            }
            part.release()
        }
    } catch (_: IOException) {
        // Ktor reports a part over `formFieldLimit` (chunked bodies have no Content-Length) and a cut-off
        // body as a plain IOException; without this it would surface as a 500.
        badRequest("Malformed or oversized multipart body")
    }
    return bytes ?: badRequest("No image part")
}

/** The file named by the `{id}` path parameter, if the caller has at least [atLeast] in its space; otherwise 404. */
private fun ApplicationCall.requireFile(
    services: Services,
    atLeast: Role
): StoredFile {
    val id = uuidOrNull(parameters["id"].orEmpty()) ?: notFound()
    val file = services.files.get(id) ?: notFound()
    requireRole(services.spaces, file.spaceId.toString(), atLeast)
    return file
}

/**
 * Images that belong to a space: editors upload and delete, any member downloads. A caller who is not a member of
 * the file's space gets 404, the same as for a file that does not exist.
 */
fun Route.fileRoutes(services: Services) {
    val store = services.store
    post("/" + ApiRoutes.files("{id}")) {
        val membership = call.requireRole(services.spaces, call.parameters["id"].orEmpty(), Role.EDITOR)
        val data = call.receiveImage()
        val mime = ImageSniffer.mimeOf(data) ?: throw ApiException(
            HttpStatusCode.UnsupportedMediaType,
            ErrorCode.UNSUPPORTED_MEDIA_TYPE,
            "Only JPEG, PNG or WebP"
        )
        val fileId = UUID.randomUUID()
        val path = store.write(membership.spaceId.toString(), fileId.toString(), data)
        val file = StoredFile(fileId, membership.spaceId, path, mime, data.size.toLong())
        // Without its row nothing would ever remove the file (the space may have been deleted meanwhile)
        runCatching { services.files.add(file, uploadedBy = membership.uid) }
            .onFailure { call.removeFiles(store, listOf(path)) }
            .getOrThrow()
        call.respond(HttpStatusCode.Created, UploadResult(fileId.toString()))
    }
    get("/" + ApiRoutes.file("{id}")) {
        val stored = call.requireFile(services, Role.VIEWER)
        val file = store.read(stored.path) ?: notFound()
        call.response.header("X-Content-Type-Options", "nosniff")
        // The type comes from the sniffed mime in the database, not from the (extension-less) file name.
        call.respond(LocalFileContent(file, ContentType.parse(stored.mime)))
    }
    delete("/" + ApiRoutes.file("{id}")) {
        val stored = call.requireFile(services, Role.EDITOR)
        if (services.files.delete(stored.id)) call.removeFiles(store, listOf(stored.path))
        call.respond(HttpStatusCode.NoContent)
    }
}
