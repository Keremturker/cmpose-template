package com.kturker.multiplatform.feature.monetization.presentation.ads

import com.kturker.multiplatform.core.ads.InterstitialPacer
import org.koin.core.annotation.Single

// The app's one pacer, created when Koin starts so that the session starts with the app. Take it where an
// interstitial shows: a ViewModel parameter (@Provided from another module) or koinInject<InterstitialPacer>().
@Single(createdAtStart = true)
fun provideInterstitialPacer(): InterstitialPacer = InterstitialPacer()
