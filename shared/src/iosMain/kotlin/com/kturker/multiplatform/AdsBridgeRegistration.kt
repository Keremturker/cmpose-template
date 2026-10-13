@file:Suppress("Filename")

package com.kturker.multiplatform

import com.kturker.multiplatform.core.ads.IosAds
import com.kturker.multiplatform.core.ads.IosAdsBridge

// Called from iOSApp.init(): the Swift AdsBridge wraps the Google Mobile Ads SDK.
fun registerAdsBridge(bridge: IosAdsBridge) = IosAds.register(bridge)
