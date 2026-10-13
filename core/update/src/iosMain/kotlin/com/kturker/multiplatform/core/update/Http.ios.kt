package com.kturker.multiplatform.core.update

import co.touchlab.kermit.Logger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import kotlin.coroutines.resume

private const val TIMEOUT_SECONDS = 10.0
private const val HTTP_OK = 200L

private val log = Logger.withTag("Update")

// The URL is not logged: it may be a private address of yours
@OptIn(BetaInteropApi::class)
internal actual suspend fun fetchText(url: String): String? {
    val nsUrl = NSURL.URLWithString(url) ?: return null
    val request = NSURLRequest.requestWithURL(
        URL = nsUrl,
        cachePolicy = NSURLRequestReloadIgnoringLocalCacheData,
        timeoutInterval = TIMEOUT_SECONDS
    )
    return suspendCancellableCoroutine { continuation ->
        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
            val status = (response as? NSHTTPURLResponse)?.statusCode
            val text = if (error == null && status == HTTP_OK && data != null) {
                NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
            } else {
                log.w { "Update check failed: ${error?.let { "error ${it.code}" } ?: "HTTP $status"}" }
                null
            }
            continuation.resume(text)
        }
        continuation.invokeOnCancellation { task.cancel() }
        task.resume()
    }
}
