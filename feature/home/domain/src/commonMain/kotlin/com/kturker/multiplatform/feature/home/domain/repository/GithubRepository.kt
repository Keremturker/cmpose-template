package com.kturker.multiplatform.feature.home.domain.repository

import com.kturker.multiplatform.core.domain.RestResult
import com.kturker.multiplatform.feature.home.domain.model.GithubUserModel

interface GithubRepository {
    suspend fun getUserDetail(username: String): RestResult<GithubUserModel>
}
