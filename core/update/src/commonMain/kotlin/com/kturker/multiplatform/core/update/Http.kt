package com.kturker.multiplatform.core.update

// A GET with the platform's own client (HttpURLConnection, NSURLSession): core/update is in every project,
// Ktor only where networking is on. Null on any failure (no connection, timeout, a status other than 200).
internal expect suspend fun fetchText(url: String): String?
