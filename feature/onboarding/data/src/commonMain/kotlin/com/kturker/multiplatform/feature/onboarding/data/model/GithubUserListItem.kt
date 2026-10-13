package com.kturker.multiplatform.feature.onboarding.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GithubUserListResponse(
    @SerialName("items")
    val items: List<GithubUserListItem>? = null
)

@Serializable
internal data class GithubUserListItem(
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("id")
    val id: Int? = null,
    @SerialName("login")
    val login: String? = null
)
