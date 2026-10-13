package com.kturker.multiplatform.feature.onboarding.data.repository

import com.kturker.multiplatform.core.domain.RestResult
import com.kturker.multiplatform.core.network.BaseRepository
import com.kturker.multiplatform.core.network.mapOnSuccess
import com.kturker.multiplatform.feature.onboarding.data.mapper.githubUserListMapper
import com.kturker.multiplatform.feature.onboarding.data.model.GithubUserListResponse
import com.kturker.multiplatform.feature.onboarding.data.service.reqUsers
import com.kturker.multiplatform.feature.onboarding.domain.model.GithubUserListModel
import com.kturker.multiplatform.feature.onboarding.domain.repository.GithubRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import org.koin.core.annotation.Single

@Single(binds = [GithubRepository::class])
internal class GithubRepositoryImpl(private val httpClient: HttpClient) :
    BaseRepository(),
    GithubRepository {
    override suspend fun getUsers(username: String): RestResult<List<GithubUserListModel>> =
        request<GithubUserListResponse> {
            httpClient.request { reqUsers(username = username) }
        }.mapOnSuccess {
            githubUserListMapper(response = it)
        }
}
