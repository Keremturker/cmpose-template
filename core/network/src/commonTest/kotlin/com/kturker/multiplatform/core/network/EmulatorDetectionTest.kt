package com.kturker.multiplatform.core.network

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmulatorDetectionTest {

    @Test
    fun sdkGphoneEmulatorIsDetected() {
        assertTrue(
            isEmulator(
                fingerprint = "google/sdk_gphone64_arm64/emu64a:16/BE2A.250530.026.D1/13818094:user/release-keys",
                model = "sdk_gphone64_arm64",
                hardware = "ranchu",
                product = "sdk_gphone64_arm64",
                manufacturer = "Google",
            ),
        )
    }

    @Test
    fun emulatorIsDetectedByProductAloneWhenOtherSignalsLookReal() {
        assertTrue(
            isEmulator(
                fingerprint = "google/husky/husky:15/AP4A.250105.002/12701944:user/release-keys",
                model = "Pixel 8 Pro",
                hardware = "husky",
                product = "sdk_gphone64_arm64",
                manufacturer = "Google",
            ),
        )
    }

    @Test
    fun oldGenericEmulatorIsDetected() {
        assertTrue(
            isEmulator(
                fingerprint = "generic/sdk_x86/generic_x86:9/PSR1.180720.075/5124027:userdebug/test-keys",
                model = "Android SDK built for x86",
                hardware = "goldfish",
                product = "sdk_x86",
                manufacturer = "unknown",
            ),
        )
    }

    @Test
    fun physicalPixelIsNotAnEmulator() {
        assertFalse(
            isEmulator(
                fingerprint = "google/husky/husky:15/AP4A.250105.002/12701944:user/release-keys",
                model = "Pixel 8 Pro",
                hardware = "husky",
                product = "husky",
                manufacturer = "Google",
            ),
        )
    }

    @Test
    fun physicalSamsungIsNotAnEmulator() {
        assertFalse(
            isEmulator(
                fingerprint = "samsung/dm3qxxx/dm3q:14/UP1A.231005.007/S918BXXU3BWK5:user/release-keys",
                model = "SM-S918B",
                hardware = "qcom",
                product = "dm3qxxx",
                manufacturer = "samsung",
            ),
        )
    }
}
