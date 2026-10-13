package com.kturker.multiplatform.core.domain

sealed class RestResult<out T> {

    class Success<out T>(val result: T) : RestResult<T>()

    data class Error(val error: DataError) : RestResult<Nothing>()

    class Loading(val loading: Boolean) : RestResult<Nothing>()
}
