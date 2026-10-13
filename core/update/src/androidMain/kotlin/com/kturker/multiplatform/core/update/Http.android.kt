package com.kturker.multiplatform.core.update

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val TIMEOUT_MILLIS = 10_000

private val log = Logger.withTag("Update")

// The URL is not logged: it may be a private address of yours
internal actual suspend fun fetchText(url: String): String? =
    withContext(context = Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as? HttpURLConnection ?: return@withContext null
            try {
                connection.connectTimeout = TIMEOUT_MILLIS
                connection.readTimeout = TIMEOUT_MILLIS
                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    log.w { "Update check failed: HTTP $status" }
                    null
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: IOException) {
            log.w { "Update check failed: ${e::class.simpleName}" }
            null
        }
    }
