package com.kturker.multiplatform.core.ads

sealed interface AdResult {
    data object Dismissed : AdResult

    data class Rewarded(val amount: Int, val type: String) : AdResult

    data class Failed(val reason: String) : AdResult
}
