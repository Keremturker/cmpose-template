package com.kturker.multiplatform.feature.home.domain.repository

import com.kturker.multiplatform.feature.home.domain.model.FavoriteUserModel
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<FavoriteUserModel>>

    fun observeIsFavorite(login: String): Flow<Boolean>

    suspend fun add(user: FavoriteUserModel)

    suspend fun remove(login: String)
}
