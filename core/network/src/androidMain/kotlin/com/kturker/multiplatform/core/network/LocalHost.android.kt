package com.kturker.multiplatform.core.network

import android.os.Build

// The emulator reaches the Mac's loopback through its 10.0.2.2 alias; a physical phone reaches it through
// `adb reverse tcp:18001 tcp:18001`, which tunnels the phone's localhost over USB.
// orEmpty(): Build's fields are null in JVM host tests.
actual fun localServerHost(): String {
    val emulator = isEmulator(
        fingerprint = Build.FINGERPRINT.orEmpty(),
        model = Build.MODEL.orEmpty(),
        hardware = Build.HARDWARE.orEmpty(),
        product = Build.PRODUCT.orEmpty(),
        manufacturer = Build.MANUFACTURER.orEmpty()
    )
    return if (emulator) "10.0.2.2" else "localhost"
}
