package com.kturker.multiplatform.feature.home.data.repository

import com.kturker.multiplatform.core.room.favorites.FavoriteUserDao
import com.kturker.multiplatform.core.room.favorites.FavoriteUserEntity
import com.kturker.multiplatform.feature.home.domain.model.FavoriteUserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class FavoritesRepositoryImplTest {

    private val dao = FakeFavoriteUserDao()
    private val repository = FavoritesRepositoryImpl(favoriteUserDao = dao)

    @Test
    fun favoritesAreTheDaoRowsAsModels() = runTest {
        dao.rows.value = listOf(
            FavoriteUserEntity(login = "torvalds", avatarUrl = "url-2", name = "Linus", addedAt = 2),
            FavoriteUserEntity(login = "octocat", avatarUrl = "url-1", name = null, addedAt = 1)
        )

        assertEquals(
            listOf(
                FavoriteUserModel(login = "torvalds", avatarUrl = "url-2", name = "Linus"),
                FavoriteUserModel(login = "octocat", avatarUrl = "url-1", name = null)
            ),
            repository.observeFavorites().first()
        )
    }

    @Test
    fun addingStoresTheUserWithTheTimeItWasAdded() = runTest {
        val before = Clock.System.now().toEpochMilliseconds()

        repository.add(user = FavoriteUserModel(login = "octocat", avatarUrl = "url", name = null))

        val after = Clock.System.now().toEpochMilliseconds()
        val stored = dao.rows.value.single()
        assertEquals("octocat", stored.login)
        assertTrue(stored.addedAt in before..after)
    }

    @Test
    fun removingDeletesByLogin() = runTest {
        repository.remove(login = "octocat")

        assertEquals(listOf("octocat"), dao.deleted)
    }
}

private class FakeFavoriteUserDao : FavoriteUserDao {
    val rows = MutableStateFlow<List<FavoriteUserEntity>>(emptyList())
    val deleted = mutableListOf<String>()

    override fun observeAll(): Flow<List<FavoriteUserEntity>> = rows

    override fun observeIsFavorite(login: String): Flow<Boolean> =
        rows.map { list -> list.any { it.login == login } }

    override suspend fun upsert(user: FavoriteUserEntity) {
        rows.value = rows.value.filterNot { it.login == user.login } + user
    }

    override suspend fun deleteByLogin(login: String) {
        deleted += login
    }
}
