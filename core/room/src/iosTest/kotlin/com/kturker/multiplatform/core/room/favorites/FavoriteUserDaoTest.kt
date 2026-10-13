package com.kturker.multiplatform.core.room.favorites

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kturker.multiplatform.core.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FavoriteUserDaoTest {

    private val database = Room.inMemoryDatabaseBuilder<AppDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
    private val dao = database.favoriteUserDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun listsTheNewestFavoriteFirst() = runTest {
        dao.upsert(favorite(login = "octocat", addedAt = 1))
        dao.upsert(favorite(login = "torvalds", addedAt = 2))

        assertEquals(listOf("torvalds", "octocat"), dao.observeAll().first().map { it.login })
    }

    @Test
    fun knowsWhetherAUserIsAFavorite() = runTest {
        assertFalse(dao.observeIsFavorite(login = "octocat").first())

        dao.upsert(favorite(login = "octocat", addedAt = 1))

        assertTrue(dao.observeIsFavorite(login = "octocat").first())
    }

    @Test
    fun savingAUserAgainReplacesItsRow() = runTest {
        val replacement = favorite(login = "octocat", addedAt = 2).copy(name = "The Octocat")
        dao.upsert(favorite(login = "octocat", addedAt = 1))
        dao.upsert(replacement)

        assertEquals(listOf(replacement), dao.observeAll().first())
    }

    @Test
    fun deletingRemovesOnlyThatUser() = runTest {
        dao.upsert(favorite(login = "octocat", addedAt = 1))
        dao.upsert(favorite(login = "torvalds", addedAt = 2))

        dao.deleteByLogin(login = "octocat")

        assertEquals(listOf("torvalds"), dao.observeAll().first().map { it.login })
    }
}

private fun favorite(
    login: String,
    addedAt: Long
) = FavoriteUserEntity(login = login, avatarUrl = "https://avatars.example/$login", name = null, addedAt = addedAt)
