package com.kturker.multiplatform.core.network

/**
 * Host the debug build reaches the Mac's local server on: 10.0.2.2 (Android emulator), localhost (physical
 * Android phone through `adb reverse tcp:18001 tcp:18001`, and the iOS simulator).
 */
expect fun localServerHost(): String
