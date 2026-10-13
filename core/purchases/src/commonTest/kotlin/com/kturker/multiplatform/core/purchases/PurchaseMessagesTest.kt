package com.kturker.multiplatform.core.purchases

import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseMessagesTest {

    @Test
    fun aUserWhoBacksOutHasCancelled() {
        assertEquals(PurchaseResult.Cancelled, purchaseResultOf(code = PurchasesErrorCode.UnknownError, userCancelled = true))
        assertEquals(PurchaseResult.Cancelled, purchaseResultOf(code = PurchasesErrorCode.PurchaseCancelledError, userCancelled = false))
    }

    @Test
    fun errorsBecomeShortTexts() {
        val cases = mapOf(
            PurchasesErrorCode.NetworkError to NETWORK_ERROR_MESSAGE,
            PurchasesErrorCode.OfflineConnectionError to NETWORK_ERROR_MESSAGE,
            PurchasesErrorCode.PaymentPendingError to PAYMENT_PENDING_MESSAGE,
            PurchasesErrorCode.StoreProblemError to STORE_PROBLEM_MESSAGE,
            PurchasesErrorCode.ProductRequestTimedOut to STORE_PROBLEM_MESSAGE,
            PurchasesErrorCode.PurchaseNotAllowedError to PURCHASE_NOT_ALLOWED_MESSAGE,
            PurchasesErrorCode.ProductNotAvailableForPurchaseError to PACKAGE_UNAVAILABLE_MESSAGE,
            PurchasesErrorCode.TestStoreSimulatedPurchaseError to PURCHASE_FAILED_MESSAGE,
            PurchasesErrorCode.UnknownError to PURCHASE_FAILED_MESSAGE
        )
        for ((code, message) in cases) {
            assertEquals(PurchaseResult.Error(message), purchaseResultOf(code = code, userCancelled = false), code.name)
        }
    }

    @Test
    fun aRestoreSucceedsOnlyWithPremium() {
        assertEquals(PurchaseResult.Success, restoreResult(premium = true))
        assertEquals(PurchaseResult.Error("No purchases to restore"), restoreResult(premium = false))
    }
}
