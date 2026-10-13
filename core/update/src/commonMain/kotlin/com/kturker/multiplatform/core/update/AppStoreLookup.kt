package com.kturker.multiplatform.core.update

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

// Only an App Store page is opened from the lookup's answer
private const val APP_STORE_URL_PREFIX = "https://apps.apple.com/"

private val lookupJson = Json { ignoreUnknownKeys = true }

// Apple's public lookup: the version on the App Store for this bundle ID, no key or server of your own
internal fun appStoreLookupUrl(bundleId: String): String =
    "https://itunes.apple.com/lookup?bundleId=$bundleId"

// An app that is not on the App Store yet has no result
internal fun parseStoreRelease(json: String): StoreRelease? = try {
    lookupJson.decodeFromString<LookupResponse>(json).results.firstOrNull()?.toStoreRelease()
} catch (_: SerializationException) {
    null
}

private fun LookupResult.toStoreRelease(): StoreRelease? {
    val storeVersion = version?.takeIf { it.isNotBlank() } ?: return null
    val url = trackViewUrl?.takeIf { it.startsWith(APP_STORE_URL_PREFIX) } ?: return null
    return StoreRelease(version = storeVersion, storeUrl = url)
}

data class StoreRelease(val version: String, val storeUrl: String)

@Serializable
private data class LookupResponse(val results: List<LookupResult> = emptyList())

@Serializable
private data class LookupResult(val version: String? = null, val trackViewUrl: String? = null)
