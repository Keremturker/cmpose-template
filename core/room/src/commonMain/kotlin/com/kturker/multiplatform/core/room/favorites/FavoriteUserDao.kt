package com.kturker.multiplatform.core.room.favorites

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteUserDao {
    @Query("SELECT * FROM favorite_users ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteUserEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_users WHERE login = :login)")
    fun observeIsFavorite(login: String): Flow<Boolean>

    @Upsert
    suspend fun upsert(user: FavoriteUserEntity)

    @Query("DELETE FROM favorite_users WHERE login = :login")
    suspend fun deleteByLogin(login: String)
}
