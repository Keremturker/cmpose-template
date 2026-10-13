package com.kturker.multiplatform.feature.monetization.presentation.paywall

import com.kturker.multiplatform.core.purchases.PaywallOffer
import com.kturker.multiplatform.core.purchases.PurchaseResult
import com.kturker.multiplatform.core.purchases.PurchasesManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class FakePurchasesManager(
    override val isAvailable: Boolean = true,
    var offerResult: PaywallOffer? = null,
    var purchaseResult: PurchaseResult = PurchaseResult.Success,
    var restoreResult: PurchaseResult = PurchaseResult.Success,
    premium: Boolean? = false
) : PurchasesManager {
    val premiumState = MutableStateFlow(premium)
    override val isPremium: StateFlow<Boolean?> = premiumState

    var refreshCalls = 0
    var offerCalls = 0
    var restoreCalls = 0
    val purchasedIds = mutableListOf<String>()

    // Set it to hold a purchase or restore open until the test completes it
    var pending: CompletableDeferred<Unit>? = null

    override suspend fun refresh() {
        refreshCalls++
    }

    override suspend fun offer(): PaywallOffer? {
        offerCalls++
        return offerResult
    }

    override suspend fun purchase(packageId: String): PurchaseResult {
        purchasedIds += packageId
        pending?.await()
        return purchaseResult
    }

    override suspend fun restore(): PurchaseResult {
        restoreCalls++
        pending?.await()
        return restoreResult
    }
}
