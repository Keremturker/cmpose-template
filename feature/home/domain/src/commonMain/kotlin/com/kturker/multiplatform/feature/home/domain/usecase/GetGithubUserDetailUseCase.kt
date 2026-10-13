package com.kturker.multiplatform.feature.home.domain.usecase

import com.kturker.multiplatform.core.domain.RestResult
import com.kturker.multiplatform.core.domain.buildDefaultFlow
import com.kturker.multiplatform.feature.home.domain.model.GithubUserModel
import com.kturker.multiplatform.feature.home.domain.repository.GithubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetGithubUserDetailUseCase(@Provided private val repository: GithubRepository) {
    operator fun invoke(username: String): Flow<RestResult<GithubUserModel>> = flow {
        val result = repository.getUserDetail(username = username)

        emit(result)
    }.buildDefaultFlow(Dispatchers.IO)
}
