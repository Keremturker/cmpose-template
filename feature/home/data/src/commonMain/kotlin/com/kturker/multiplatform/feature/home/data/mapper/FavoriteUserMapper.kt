package com.kturker.multiplatform.feature.home.data.mapper

import com.kturker.multiplatform.core.room.favorites.FavoriteUserEntity
import com.kturker.multiplatform.feature.home.domain.model.FavoriteUserModel

internal fun FavoriteUserEntity.toFavoriteUserModel(): FavoriteUserModel =
    FavoriteUserModel(login = login, avatarUrl = avatarUrl, name = name)

internal fun FavoriteUserModel.toFavoriteUserEntity(addedAt: Long): FavoriteUserEntity =
    FavoriteUserEntity(login = login, avatarUrl = avatarUrl, name = name, addedAt = addedAt)
