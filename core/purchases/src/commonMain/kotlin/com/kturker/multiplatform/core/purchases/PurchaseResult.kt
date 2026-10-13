package com.kturker.multiplatform.core.purchases

sealed interface PurchaseResult {
    // isPremium follows on its own
    data object Success : PurchaseResult

    // The user backed out of the store's sheet: show nothing
    data object Cancelled : PurchaseResult

    // A short text the screen can show as it is
    data class Error(val message: String) : PurchaseResult
}
