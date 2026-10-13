package com.kturker.multiplatform.core.room.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_users")
data class FavoriteUserEntity(
    @PrimaryKey val login: String,
    val avatarUrl: String,
    val name: String?,
    val addedAt: Long
)
