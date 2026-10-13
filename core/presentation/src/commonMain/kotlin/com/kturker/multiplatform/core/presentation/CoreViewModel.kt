package com.kturker.multiplatform.core.presentation

import androidx.lifecycle.ViewModel
import com.kturker.multiplatform.core.domain.RestResult
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent

abstract class CoreViewModel :
    ViewModel(),
    KoinComponent {

    // Failures arrive as RestResult.Error: the use case's buildDefaultFlow catches them, and lets a
    // cancellation through, so a cancelled request never shows as an error
    fun <T : Any> safeFlowApiCall(call: () -> Flow<RestResult<T>>): Flow<RestResult<T>> =
        call.invoke()
}
