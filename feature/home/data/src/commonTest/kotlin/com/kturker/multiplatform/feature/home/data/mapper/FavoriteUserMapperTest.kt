package com.kturker.multiplatform.feature.home.data.mapper

import com.kturker.multiplatform.core.room.favorites.FavoriteUserEntity
import com.kturker.multiplatform.feature.home.domain.model.FavoriteUserModel
import kotlin.test.Test
import kotlin.test.assertEquals

class FavoriteUserMapperTest {

    @Test
    fun mapsAnEntityToTheModel() {
        val entity = FavoriteUserEntity(login = "octocat", avatarUrl = "url", name = "The Octocat", addedAt = 5)

        assertEquals(
            FavoriteUserModel(login = "octocat", avatarUrl = "url", name = "The Octocat"),
            entity.toFavoriteUserModel()
        )
    }

    @Test
    fun mapsTheModelToAnEntityWithTheTimeItWasAdded() {
        val model = FavoriteUserModel(login = "octocat", avatarUrl = "url", name = null)

        val entity = model.toFavoriteUserEntity(addedAt = 7)

        assertEquals("octocat", entity.login)
        assertEquals("url", entity.avatarUrl)
        assertEquals(null, entity.name)
        assertEquals(7, entity.addedAt)
    }
}
