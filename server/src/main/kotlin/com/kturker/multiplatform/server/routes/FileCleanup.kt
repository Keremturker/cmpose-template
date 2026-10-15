package com.kturker.multiplatform.server.routes

import com.kturker.multiplatform.server.describe
import com.kturker.multiplatform.server.files.FileStore
import io.ktor.server.application.ApplicationCall
import java.util.UUID

/**
 * Removes files from disk after their rows are gone. A failure does not fail the request: it is logged by file id
 * (the last segment of a stored path) and exception class names only, never a uid or the exception's message.
 */
internal fun ApplicationCall.removeFiles(
    store: FileStore,
    paths: List<String>
) {
    val log = application.environment.log
    paths.forEach { path ->
        runCatching { store.delete(path) }
            .onFailure { log.warn("Could not delete file {}: {}", path.substringAfterLast('/'), describe(it)) }
    }
}

/** Removes the folders of deleted spaces; a failure is logged (exception class names only) and does not fail. */
internal fun ApplicationCall.removeSpaceDirs(
    store: FileStore,
    spaceIds: List<UUID>
) {
    val log = application.environment.log
    spaceIds.forEach { spaceId ->
        runCatching { store.deleteSpaceDir(spaceId.toString()) }
            .onFailure { log.warn("Could not delete a space's file folder: {}", describe(it)) }
    }
}
