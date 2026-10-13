package com.kturker.multiplatform.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

fun <T> Flow<RestResult<T>>.onSuccess(action: suspend (T) -> Unit): Flow<RestResult<T>> =
    transform { restResult ->
        if (restResult is RestResult.Success) {
            action.invoke(restResult.result)
        }
        emit(restResult)
    }

fun <T> Flow<RestResult<T>>.onError(action: suspend (DataError) -> Unit): Flow<RestResult<T>> =
    transform { restResult ->
        if (restResult is RestResult.Error) {
            action.invoke(restResult.error)
        }
        emit(restResult)
    }

fun <T> Flow<RestResult<T>>.onLoading(action: suspend (Boolean) -> Unit): Flow<RestResult<T>> =
    transform { restResult ->
        if (restResult is RestResult.Loading) {
            action.invoke(restResult.loading)
        }
        emit(restResult)
    }
