package com.kturker.multiplatform.feature.home.domain.usecase

import com.kturker.multiplatform.feature.home.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class ObserveIsFavoriteUseCase(@Provided private val repository: FavoritesRepository) {
    operator fun invoke(login: String): Flow<Boolean> = repository.observeIsFavorite(login = login)
}
