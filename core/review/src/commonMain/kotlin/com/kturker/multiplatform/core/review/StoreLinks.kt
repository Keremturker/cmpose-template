package com.kturker.multiplatform.core.review

internal fun appStoreWriteReviewUrl(appStoreId: String): String? =
    appStoreId.trim().takeIf { it.isNotEmpty() }?.let { "https://apps.apple.com/app/id$it?action=write-review" }
