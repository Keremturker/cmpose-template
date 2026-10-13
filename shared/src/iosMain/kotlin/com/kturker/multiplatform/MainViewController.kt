package com.kturker.multiplatform

import androidx.compose.ui.window.ComposeUIViewController
import com.kturker.multiplatform.di.initKoin
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * Starts Koin. The iOS app calls this once, from `iOSApp.init`: Koin can start only once
 * per process, so it must not start in [MainViewController], which runs for every window.
 */
@OptIn(ExperimentalNativeApi::class)
fun startKoinAtLaunch() = initKoin(isDebug = Platform.isDebugBinary)

@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController {
    MainScreen()
}
