package com.kturker.multiplatform.feature.monetization.presentation.paywall

import com.kturker.multiplatform.core.about.LegalLink
import com.kturker.multiplatform.core.purchases.PackagePeriod
import com.kturker.multiplatform.core.purchases.PaywallOffer
import com.kturker.multiplatform.core.purchases.PaywallPackage

internal data class PaywallUiState(
    // False in a release build with a Test Store key: RevenueCat is not configured, nothing can be bought
    val isAvailable: Boolean,
    val loading: Boolean = isAvailable,
    val offer: PaywallOffer? = null,
    val selectedPackageId: String? = null,
    // A purchase or restore is running: the buttons wait for its result
    val inProgress: Boolean = false,
    val error: String? = null,
    // Privacy policy and terms (core/about): the stores want them reachable from a paywall
    val legalLinks: List<LegalLink> = emptyList()
) {
    val packages: List<PaywallPackage> get() = offer?.packages.orEmpty()

    val canPurchase: Boolean get() = selectedPackageId != null && !inProgress
}

// The plan selected first: ANNUAL when the offering has one (usually the best value), else the first one
internal fun defaultSelection(packages: List<PaywallPackage>): String? =
    (packages.firstOrNull { it.period == PackagePeriod.ANNUAL } ?: packages.firstOrNull())?.id
