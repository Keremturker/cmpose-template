package com.kturker.multiplatform.feature.onboarding.domain.usecase

import com.kturker.multiplatform.core.domain.RestResult
import com.kturker.multiplatform.core.domain.buildDefaultFlow
import com.kturker.multiplatform.feature.onboarding.domain.model.GithubUserListModel
import com.kturker.multiplatform.feature.onboarding.domain.repository.GithubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class SearchGithubUsersUseCase(@Provided private val repository: GithubRepository) {
    operator fun invoke(username: String): Flow<RestResult<List<GithubUserListModel>>> = flow {
        val result = repository.getUsers(username = username)

        emit(result)
    }.buildDefaultFlow(Dispatchers.IO)
}
