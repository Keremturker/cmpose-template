package com.kturker.multiplatform.core.network

import com.kturker.multiplatform.core.domain.RestResult

inline fun <T, R> RestResult<T>.mapOnSuccess(map: (T?) -> R): RestResult<R> = when (this) {
    is RestResult.Success -> RestResult.Success(map(result))
    is RestResult.Error -> this
    is RestResult.Loading -> this
}
