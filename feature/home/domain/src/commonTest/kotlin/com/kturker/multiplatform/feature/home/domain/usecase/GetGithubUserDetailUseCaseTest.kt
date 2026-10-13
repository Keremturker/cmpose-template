package com.kturker.multiplatform.feature.home.domain.usecase

import app.cash.turbine.test
import com.kturker.multiplatform.core.domain.DataError
import com.kturker.multiplatform.core.domain.RestResult
import com.kturker.multiplatform.feature.home.domain.model.GithubUserModel
import com.kturker.multiplatform.feature.home.domain.repository.GithubRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class GetGithubUserDetailUseCaseTest {

    @Test
    fun emitsLoadingThenTheUserThenLoadingDone() = runTest {
        val user = githubUser(login = "octocat")
        val requested = mutableListOf<String>()
        val useCase = GetGithubUserDetailUseCase(
            repository = FakeGithubRepository { username ->
                requested += username
                RestResult.Success(user)
            }
        )

        useCase(username = "octocat").test {
            assertEquals(true, assertIs<RestResult.Loading>(awaitItem()).loading)
            assertSame(user, assertIs<RestResult.Success<*>>(awaitItem()).result)
            assertEquals(false, assertIs<RestResult.Loading>(awaitItem()).loading)
            awaitComplete()
        }
        assertEquals(listOf("octocat"), requested)
    }

    @Test
    fun turnsAFailedRequestIntoAnError() = runTest {
        val useCase = GetGithubUserDetailUseCase(
            repository = FakeGithubRepository { error("offline") }
        )

        useCase(username = "octocat").test {
            assertEquals(true, assertIs<RestResult.Loading>(awaitItem()).loading)
            val error = assertIs<RestResult.Error>(awaitItem()).error
            assertEquals("offline", assertIs<DataError.Unknown>(error).cause.message)
            assertEquals(false, assertIs<RestResult.Loading>(awaitItem()).loading)
            awaitComplete()
        }
    }
}

private class FakeGithubRepository(
    private val onGetUserDetail: suspend (username: String) -> RestResult<GithubUserModel>
) : GithubRepository {
    override suspend fun getUserDetail(username: String): RestResult<GithubUserModel> =
        onGetUserDetail(username)
}

private fun githubUser(login: String) = GithubUserModel(
    login = login,
    id = 1,
    nodeId = null,
    avatarUrl = null,
    gravatarId = null,
    url = null,
    htmlUrl = null,
    followersUrl = null,
    followingUrl = null,
    gistsUrl = null,
    starredUrl = null,
    subscriptionsUrl = null,
    organizationsUrl = null,
    reposUrl = null,
    eventsUrl = null,
    receivedEventsUrl = null,
    type = null,
    siteAdmin = null,
    name = null,
    company = null,
    blog = null,
    location = null,
    email = null,
    hireable = null,
    bio = null,
    twitterUsername = null,
    publicRepos = null,
    publicGists = null,
    followers = null,
    following = null,
    createdAt = null,
    updatedAt = null
)
