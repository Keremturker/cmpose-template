package com.kturker.multiplatform.feature.home.data.repository

import com.kturker.multiplatform.core.room.favorites.FavoriteUserDao
import com.kturker.multiplatform.feature.home.data.mapper.toFavoriteUserEntity
import com.kturker.multiplatform.feature.home.data.mapper.toFavoriteUserModel
import com.kturker.multiplatform.feature.home.domain.model.FavoriteUserModel
import com.kturker.multiplatform.feature.home.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import kotlin.time.Clock

@Single(binds = [FavoritesRepository::class])
class FavoritesRepositoryImpl(@Provided private val favoriteUserDao: FavoriteUserDao) : FavoritesRepository {
    override fun observeFavorites(): Flow<List<FavoriteUserModel>> =
        favoriteUserDao.observeAll().map { rows -> rows.map { it.toFavoriteUserModel() } }

    override fun observeIsFavorite(login: String): Flow<Boolean> =
        favoriteUserDao.observeIsFavorite(login = login)

    override suspend fun add(user: FavoriteUserModel) {
        favoriteUserDao.upsert(user = user.toFavoriteUserEntity(addedAt = Clock.System.now().toEpochMilliseconds()))
    }

    override suspend fun remove(login: String) {
        favoriteUserDao.deleteByLogin(login = login)
    }
}
