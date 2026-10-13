package com.kturker.multiplatform.feature.monetization.presentation.ads

import com.kturker.multiplatform.core.purchases.PaywallOffer
import com.kturker.multiplatform.core.purchases.PurchaseResult
import com.kturker.multiplatform.core.purchases.PurchasesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PremiumAdFreeSourceTest {

    @Test
    fun collectingAsksRevenueCatOnce() = runTest {
        val purchasesManager = FakePurchasesManager(premiumAfterRefresh = true)

        assertEquals(true, PremiumAdFreeSource(purchasesManager).isAdFree.first())
        assertEquals(1, purchasesManager.refreshCount)
    }

    @Test
    fun noAnswerKeepsPremiumUnknown() = runTest {
        val purchasesManager = FakePurchasesManager(premiumAfterRefresh = null)

        assertNull(PremiumAdFreeSource(purchasesManager).isAdFree.first())
        assertEquals(1, purchasesManager.refreshCount)
    }
}

private class FakePurchasesManager(private val premiumAfterRefresh: Boolean?) : PurchasesManager {
    var refreshCount = 0
    private val premium = MutableStateFlow<Boolean?>(null)
    override val isAvailable: Boolean = true
    override val isPremium: StateFlow<Boolean?> = premium

    override suspend fun refresh() {
        refreshCount++
        premium.value = premiumAfterRefresh
    }

    override suspend fun offer(): PaywallOffer? = null

    override suspend fun purchase(packageId: String): PurchaseResult = PurchaseResult.Cancelled

    override suspend fun restore(): PurchaseResult = PurchaseResult.Cancelled
}
