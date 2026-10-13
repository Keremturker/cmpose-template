package com.kturker.multiplatform.feature.home.domain.usecase

import app.cash.turbine.test
import com.kturker.multiplatform.feature.home.domain.model.FavoriteUserModel
import com.kturker.multiplatform.feature.home.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoriteUseCasesTest {

    private val octocat = FavoriteUserModel(login = "octocat", avatarUrl = "https://avatars.example/1", name = null)
    private val repository = FakeFavoritesRepository()

    @Test
    fun observingFavoritesFollowsTheRepository() = runTest {
        val users = listOf(octocat, octocat.copy(login = "torvalds"))
        repository.favorites.value = users

        assertEquals(users, ObserveFavoritesUseCase(repository = repository)().first())
    }

    @Test
    fun observingOneUserTellsWhetherItIsAFavorite() = runTest {
        ObserveIsFavoriteUseCase(repository = repository)(login = "octocat").test {
            assertEquals(false, awaitItem())

            repository.favorites.value = listOf(octocat)

            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun togglingANonFavoriteAddsIt() = runTest {
        ToggleFavoriteUseCase(repository = repository)(user = octocat, isFavorite = false)

        assertEquals(listOf(octocat), repository.added)
        assertTrue(repository.removed.isEmpty())
    }

    @Test
    fun togglingAFavoriteRemovesIt() = runTest {
        ToggleFavoriteUseCase(repository = repository)(user = octocat, isFavorite = true)

        assertEquals(listOf("octocat"), repository.removed)
        assertTrue(repository.added.isEmpty())
    }
}

private class FakeFavoritesRepository : FavoritesRepository {
    val favorites = MutableStateFlow<List<FavoriteUserModel>>(emptyList())
    val added = mutableListOf<FavoriteUserModel>()
    val removed = mutableListOf<String>()

    override fun observeFavorites(): Flow<List<FavoriteUserModel>> = favorites

    override fun observeIsFavorite(login: String): Flow<Boolean> =
        favorites.map { users -> users.any { it.login == login } }

    override suspend fun add(user: FavoriteUserModel) {
        added += user
    }

    override suspend fun remove(login: String) {
        removed += login
    }
}
