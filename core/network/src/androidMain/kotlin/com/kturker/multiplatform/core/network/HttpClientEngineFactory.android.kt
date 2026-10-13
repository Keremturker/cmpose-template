package com.kturker.multiplatform.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual fun getPlatformEngine(): HttpClientEngine = Android.create()
