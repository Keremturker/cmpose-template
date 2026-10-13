package com.kturker.multiplatform.feature.home.data.mapper

import com.kturker.multiplatform.feature.home.data.model.GithubUserResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class GithubUserMapperTest {

    // Same leniency as the app's HttpClient: fields the model does not declare are ignored.
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapsTheFieldsOfAGithubUserResponse() {
        val response = json.decodeFromString<GithubUserResponse>(
            """
            {
              "login": "octocat",
              "id": 583231,
              "avatar_url": "https://avatars.githubusercontent.com/u/583231",
              "html_url": "https://github.com/octocat",
              "name": "The Octocat",
              "twitter_username": "github",
              "public_repos": 8,
              "followers": 21000,
              "site_admin": false,
              "plan": { "name": "free" }
            }
            """.trimIndent()
        )

        val user = githubUserMapper(response = response)

        assertEquals("octocat", user.login)
        assertEquals(583231, user.id)
        assertEquals("https://avatars.githubusercontent.com/u/583231", user.avatarUrl)
        assertEquals("https://github.com/octocat", user.htmlUrl)
        assertEquals("The Octocat", user.name)
        assertEquals("github", user.twitterUsername)
        assertEquals(8, user.publicRepos)
        assertEquals(21000, user.followers)
        assertEquals(false, user.siteAdmin)
    }

    @Test
    fun fillsFieldsTheApiLeftOutWithEmptyValues() {
        val user = githubUserMapper(response = GithubUserResponse(login = "octocat"))

        assertEquals("octocat", user.login)
        assertEquals(0, user.id)
        assertEquals("", user.bio)
        assertEquals("", user.company)
        assertEquals(0, user.followers)
        assertEquals(false, user.hireable)
    }

    @Test
    fun mapsAMissingResponseToAnEmptyUser() {
        val user = githubUserMapper(response = null)

        assertEquals("", user.login)
        assertEquals(0, user.id)
    }
}
