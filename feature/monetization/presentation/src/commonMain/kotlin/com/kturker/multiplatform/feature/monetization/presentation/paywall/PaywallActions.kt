package com.kturker.multiplatform.feature.monetization.presentation.paywall

internal interface PaywallActions {
    fun onDismiss()

    fun onPackageSelected(packageId: String)

    fun onPurchaseClick()

    fun onRestoreClick()

    fun onRetry()
}
