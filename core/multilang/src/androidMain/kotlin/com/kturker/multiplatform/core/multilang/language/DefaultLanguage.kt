package com.kturker.multiplatform.core.multilang.language

import java.util.Locale

actual fun getDeviceLanguageCode(): String = Locale.getDefault().language
