package com.kturker.multiplatform.core.network

import io.ktor.client.engine.HttpClientEngine

expect fun getPlatformEngine(): HttpClientEngine
