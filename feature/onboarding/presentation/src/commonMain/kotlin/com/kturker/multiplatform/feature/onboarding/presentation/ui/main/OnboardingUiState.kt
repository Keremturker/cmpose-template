package com.kturker.multiplatform.feature.onboarding.presentation.ui.main

import com.kturker.multiplatform.core.domain.DataError
import com.kturker.multiplatform.feature.home.domain.model.FavoriteUserModel
import com.kturker.multiplatform.feature.onboarding.domain.model.GithubUserListModel

internal data class OnboardingUiState(
    val query: String = "",
    val users: List<GithubUserListModel> = listOf(),
    val isLoading: Boolean = false,
    val error: DataError? = null,
    val favorites: List<FavoriteUserModel> = listOf()
)
