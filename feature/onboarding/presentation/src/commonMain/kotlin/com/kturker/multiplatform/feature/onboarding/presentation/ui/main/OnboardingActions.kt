package com.kturker.multiplatform.feature.onboarding.presentation.ui.main

import com.kturker.multiplatform.feature.home.domain.model.FavoriteUserModel
import com.kturker.multiplatform.feature.onboarding.domain.model.GithubUserListModel

internal interface OnboardingActions {
    fun searchUser()
    fun onQueryChanged(username: String)
    fun onRetry()
    fun onNavigateToDetail(user: GithubUserListModel)
    fun onNavigateToSettings()
    fun onFavoriteClick(favorite: FavoriteUserModel)
    fun onRemoveFavorite(favorite: FavoriteUserModel)
}
