package com.kturker.multiplatform.feature.monetization.presentation.ads

import kotlinx.coroutines.flow.Flow

// Whether ads are hidden for this user. With in-app purchases (the purchases module, RevenueCat) it follows
// the "premium" entitlement; without them, ads always show (the generator swaps in NoPremiumAdFreeSource).
internal interface AdFreeSource {
    val isAdFree: Flow<Boolean?>
}
