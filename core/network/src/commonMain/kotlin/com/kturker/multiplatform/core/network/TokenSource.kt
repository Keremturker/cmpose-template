package com.kturker.multiplatform.core.network

/** Supplies the signed-in user's ID token for backend calls; implemented by core:auth, faked in tests. */
interface TokenSource {
    suspend fun token(forceRefresh: Boolean): String?
}
