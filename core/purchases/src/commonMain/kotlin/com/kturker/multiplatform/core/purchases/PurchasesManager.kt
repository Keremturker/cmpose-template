package com.kturker.multiplatform.core.purchases

import kotlinx.coroutines.flow.StateFlow

const val PREMIUM_ENTITLEMENT = "premium"

fun hasPremium(activeEntitlementIds: Set<String>): Boolean =
    PREMIUM_ENTITLEMENT in activeEntitlementIds

// Unknown (null) until RevenueCat answers; false without RevenueCat, where nobody is premium.
internal fun initialPremium(isAvailable: Boolean): Boolean? = if (isAvailable) null else false

interface PurchasesManager {
    // False when RevenueCat is not configured (a release build with a Test Store key): nobody is premium.
    val isAvailable: Boolean

    // Null while unknown (RevenueCat has not answered yet: first launch, offline): ads wait, nothing unlocks.
    val isPremium: StateFlow<Boolean?>

    suspend fun refresh()

    // The current offering for a paywall: null without RevenueCat, without a current offering or when it cannot
    // be loaded (offline)
    suspend fun offer(): PaywallOffer?

    // Buys a package of the offer by its id; Success also updates isPremium
    suspend fun purchase(packageId: String): PurchaseResult

    // Restores earlier purchases; an Error when none brings premium back
    suspend fun restore(): PurchaseResult
}
