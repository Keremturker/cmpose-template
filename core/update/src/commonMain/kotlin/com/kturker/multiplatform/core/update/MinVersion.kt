package com.kturker.multiplatform.core.update

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val minVersionJson = Json { ignoreUnknownKeys = true }

@Serializable
internal data class MinVersionResponse(val minVersion: MinVersions)

// The oldest version still supported: android is a versionCode, ios a CFBundleShortVersionString
@Serializable
data class MinVersions(val android: Int? = null, val ios: String? = null)

// A broken or unexpected file means no forced update
internal fun parseMinVersions(json: String): MinVersions? = try {
    minVersionJson.decodeFromString<MinVersionResponse>(json).minVersion
} catch (_: SerializationException) {
    null
}
