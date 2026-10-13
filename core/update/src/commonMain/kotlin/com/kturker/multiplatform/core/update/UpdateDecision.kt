package com.kturker.multiplatform.core.update

import com.kturker.multiplatform.core.about.AppVersion

enum class UpdatePlatform {
    Android,
    Ios
}

// storeUrl: the App Store page on iOS; null on Android, where "Update" opens the Play Store page itself
sealed interface UpdateDecision {
    data object None : UpdateDecision

    data class Available(val storeUrl: String?) : UpdateDecision

    data class Required(val storeUrl: String?) : UpdateDecision
}

// Below the minimum version: Required. A newer version on the App Store: Available (iOS; Android's recommended
// update is Play's own flow and is not decided here). Anything unknown (no file, no lookup result): None.
fun decideUpdate(
    current: AppVersion,
    minVersions: MinVersions?,
    storeRelease: StoreRelease?,
    platform: UpdatePlatform
): UpdateDecision {
    val storeUrl = storeRelease?.storeUrl
    val belowMinimum = if (platform == UpdatePlatform.Android) {
        isBelowAndroidMinimum(versionCode = current.code, minimum = minVersions?.android)
    } else {
        isBelowIosMinimum(versionName = current.name, minimum = minVersions?.ios)
    }
    return when {
        belowMinimum -> UpdateDecision.Required(storeUrl = storeUrl)

        storeRelease != null && compareVersionNames(storeRelease.version, current.name) > 0 ->
            UpdateDecision.Available(storeUrl = storeUrl)

        else -> UpdateDecision.None
    }
}

private fun isBelowAndroidMinimum(
    versionCode: String,
    minimum: Int?
): Boolean {
    val code = versionCode.toLongOrNull() ?: return false
    return minimum != null && code < minimum
}

private fun isBelowIosMinimum(
    versionName: String,
    minimum: String?
): Boolean =
    versionName.isNotBlank() && !minimum.isNullOrBlank() && compareVersionNames(versionName, minimum) < 0
