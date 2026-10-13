package com.kturker.multiplatform.feature.monetization.presentation.ads

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Single

// No in-app purchases in this project, so nobody is ad-free. Add the purchases module (RevenueCat) and bind
// AdFreeSource to its "premium" entitlement to offer an ad-free upgrade.
@Single(binds = [AdFreeSource::class])
internal class NoPremiumAdFreeSource : AdFreeSource {
    override val isAdFree: Flow<Boolean?> = flowOf(false)
}
