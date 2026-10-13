package com.kturker.multiplatform.feature.home.domain.usecase

import com.kturker.multiplatform.feature.home.domain.model.FavoriteUserModel
import com.kturker.multiplatform.feature.home.domain.repository.FavoritesRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

// The star on a profile and the remove button in the favorites list: the caller knows the current state
@Factory
class ToggleFavoriteUseCase(@Provided private val repository: FavoritesRepository) {
    suspend operator fun invoke(
        user: FavoriteUserModel,
        isFavorite: Boolean
    ) {
        if (isFavorite) repository.remove(login = user.login) else repository.add(user = user)
    }
}
