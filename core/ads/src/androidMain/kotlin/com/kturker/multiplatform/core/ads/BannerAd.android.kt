package com.kturker.multiplatform.core.ads

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd as GmaBannerAd

// A fixed 320x50 banner, like the iOS one. The AdView loads its ad once and is destroyed when the banner leaves.
@Composable
actual fun BannerAd(modifier: Modifier) {
    val activity = requireNotNull(LocalActivity.current) { "Ads need the hosting Activity" }
    val request = remember {
        BannerAdRequest.Builder(AdUnitIds.banner, AdSize.BANNER).build()
    }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = {
                AdView(activity).apply {
                    loadAd(
                        request,
                        object : AdLoadCallback<GmaBannerAd> {
                            // A failed load leaves the slot empty; there is no logger in the template yet.
                            override fun onAdFailedToLoad(adError: LoadAdError) = Unit
                        }
                    )
                }
            },
            onRelease = { it.destroy() }
        )
    }
}
