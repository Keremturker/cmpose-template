package com.kturker.multiplatform.feature.home.presentation.ui

import com.kturker.multiplatform.core.domain.DataError
import com.kturker.multiplatform.feature.home.domain.model.GithubUserModel

internal data class HomeUiState(
    val user: GithubUserModel? = null,
    val isLoading: Boolean = false,
    val error: DataError? = null,
    val isFavorite: Boolean = false
)
