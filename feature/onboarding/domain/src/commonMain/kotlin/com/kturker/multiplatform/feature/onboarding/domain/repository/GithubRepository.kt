package com.kturker.multiplatform.feature.onboarding.domain.repository

import com.kturker.multiplatform.core.domain.RestResult
import com.kturker.multiplatform.feature.onboarding.domain.model.GithubUserListModel

interface GithubRepository {
    suspend fun getUsers(username: String): RestResult<List<GithubUserListModel>>
}
