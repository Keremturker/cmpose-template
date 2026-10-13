package com.kturker.multiplatform.core.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.cancellation.CancellationException

inline fun <reified T> Flow<RestResult<T>>.buildDefaultFlow(
    dispatcher: CoroutineDispatcher,
    loading: Boolean = true
): Flow<RestResult<T>> = this.onStart {
    emit(RestResult.Loading(loading))
}.catch { cause ->
    // A cancelled request is not a failure: it goes on up instead of becoming an Error
    if (cause is CancellationException) throw cause
    emit(RestResult.Error(DataError.Unknown(cause = cause)))
}.onCompletion {
    emit(RestResult.Loading(false))
}.flowOn(dispatcher)
