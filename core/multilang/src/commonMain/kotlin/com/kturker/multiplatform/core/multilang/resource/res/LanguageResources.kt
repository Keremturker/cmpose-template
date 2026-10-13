package com.kturker.multiplatform.core.multilang.resource.res

import com.kturker.multiplatform.core.multilang.language.AppLanguage
import com.kturker.multiplatform.core.multilang.resource.StringResourcesUiModel

internal val languageResources = mapOf(
    AppLanguage.EN to resourceEN,
    AppLanguage.TR to resourceTR,
    AppLanguage.ES to resourceES,
    AppLanguage.IT to resourceIT,
    AppLanguage.FR to resourceFR
)

/** English: shown for device languages without a translation and until the saved language loads. */
val fallbackResources: StringResourcesUiModel
    get() = resourceEN
