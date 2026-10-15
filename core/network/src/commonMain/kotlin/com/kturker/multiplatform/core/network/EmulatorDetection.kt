package com.kturker.multiplatform.core.network

/**
 * True when the given android.os.Build values describe an Android emulator rather than a physical device.
 * Pure so it can be tested with recorded values from both kinds of device.
 */
internal fun isEmulator(
    fingerprint: String,
    model: String,
    hardware: String,
    product: String,
    manufacturer: String
): Boolean = hardware == "ranchu" ||
    hardware == "goldfish" ||
    fingerprint.startsWith("generic") ||
    fingerprint.contains("emulator") ||
    fingerprint.contains("sdk_gphone") ||
    product.contains("sdk") ||
    product.contains("emulator") ||
    model.contains("Emulator") ||
    model.contains("Android SDK built for") ||
    manufacturer.contains("Genymotion")
