package com.kturker.multiplatform.feature.onboarding.data.mapper

import com.kturker.multiplatform.feature.onboarding.data.model.GithubUserListResponse
import com.kturker.multiplatform.feature.onboarding.domain.model.GithubUserListModel

internal fun githubUserListMapper(response: GithubUserListResponse?) =
    response?.items?.map { item ->
        GithubUserListModel(
            avatarUrl = item.avatarUrl.orEmpty(),
            id = item.id ?: 0,
            login = item.login.orEmpty()
        )
    }.orEmpty()
