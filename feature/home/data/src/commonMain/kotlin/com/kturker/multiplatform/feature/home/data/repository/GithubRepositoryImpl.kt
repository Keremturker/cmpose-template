package com.kturker.multiplatform.feature.home.data.repository

import com.kturker.multiplatform.core.domain.RestResult
import com.kturker.multiplatform.core.network.BaseRepository
import com.kturker.multiplatform.core.network.mapOnSuccess
import com.kturker.multiplatform.feature.home.data.mapper.githubUserMapper
import com.kturker.multiplatform.feature.home.data.model.GithubUserResponse
import com.kturker.multiplatform.feature.home.data.service.reqUserDetail
import com.kturker.multiplatform.feature.home.domain.model.GithubUserModel
import com.kturker.multiplatform.feature.home.domain.repository.GithubRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import org.koin.core.annotation.Single

@Single(binds = [GithubRepository::class])
class GithubRepositoryImpl(private val httpClient: HttpClient) :
    BaseRepository(),
    GithubRepository {
    override suspend fun getUserDetail(username: String): RestResult<GithubUserModel> =
        request<GithubUserResponse> {
            httpClient.request { reqUserDetail(username = username) }
        }.mapOnSuccess {
            githubUserMapper(response = it)
        }
}
