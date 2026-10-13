package com.kturker.multiplatform.feature.monetization.presentation.ads

import com.kturker.multiplatform.core.purchases.PurchasesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onSubscription
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

// Asks RevenueCat once per collector; until it answers (or when offline without a cached answer) the
// state stays unknown and the ads screen shows no banner or interstitial.
@Single(binds = [AdFreeSource::class])
internal class PremiumAdFreeSource(@Provided private val purchasesManager: PurchasesManager) : AdFreeSource {
    override val isAdFree: Flow<Boolean?> = purchasesManager.isPremium.onSubscription { purchasesManager.refresh() }
}
