package com.kturker.multiplatform.core.purchases

import com.revenuecat.purchases.kmp.models.PurchasesErrorCode

// The texts PurchaseResult.Error carries. English like the template's other monetization screens: core/purchases
// does not depend on core/multilang, which a project may not have. Translate them where you show them.
internal const val PURCHASES_UNAVAILABLE_MESSAGE = "Purchases are not available in this build"
internal const val PACKAGE_UNAVAILABLE_MESSAGE = "This plan is not available right now. Try again later."
internal const val NO_PURCHASES_TO_RESTORE_MESSAGE = "No purchases to restore"
internal const val NETWORK_ERROR_MESSAGE = "No connection. Check your internet and try again."
internal const val PAYMENT_PENDING_MESSAGE = "Payment pending: premium unlocks once the store confirms it."
internal const val STORE_PROBLEM_MESSAGE = "The store is not responding. Try again later."
internal const val PURCHASE_NOT_ALLOWED_MESSAGE = "Purchases are not allowed on this device."
internal const val PURCHASE_FAILED_MESSAGE = "Purchase failed. Try again."

// A purchase or restore that did not go through, as the paywall shows it
internal fun purchaseResultOf(
    code: PurchasesErrorCode,
    userCancelled: Boolean
): PurchaseResult = if (userCancelled || code == PurchasesErrorCode.PurchaseCancelledError) {
    PurchaseResult.Cancelled
} else {
    PurchaseResult.Error(errorMessage(code))
}

private fun errorMessage(code: PurchasesErrorCode): String = when (code) {
    PurchasesErrorCode.NetworkError, PurchasesErrorCode.OfflineConnectionError -> NETWORK_ERROR_MESSAGE
    PurchasesErrorCode.PaymentPendingError -> PAYMENT_PENDING_MESSAGE
    PurchasesErrorCode.StoreProblemError, PurchasesErrorCode.ProductRequestTimedOut -> STORE_PROBLEM_MESSAGE
    PurchasesErrorCode.PurchaseNotAllowedError -> PURCHASE_NOT_ALLOWED_MESSAGE
    PurchasesErrorCode.ProductNotAvailableForPurchaseError -> PACKAGE_UNAVAILABLE_MESSAGE
    else -> PURCHASE_FAILED_MESSAGE
}

// A restore succeeds only when it brings premium back
internal fun restoreResult(premium: Boolean): PurchaseResult =
    if (premium) PurchaseResult.Success else PurchaseResult.Error(NO_PURCHASES_TO_RESTORE_MESSAGE)
