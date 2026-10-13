package com.kturker.multiplatform.feature.home.data.mapper

import com.kturker.multiplatform.feature.home.data.model.GithubUserResponse
import com.kturker.multiplatform.feature.home.domain.model.GithubUserModel

internal fun githubUserMapper(response: GithubUserResponse?): GithubUserModel = GithubUserModel(
    login = response?.login.orEmpty(),
    id = response?.id ?: 0,
    nodeId = response?.nodeId.orEmpty(),
    avatarUrl = response?.avatarUrl.orEmpty(),
    gravatarId = response?.gravatarId.orEmpty(),
    url = response?.url.orEmpty(),
    htmlUrl = response?.htmlUrl.orEmpty(),
    followersUrl = response?.followersUrl.orEmpty(),
    followingUrl = response?.followingUrl.orEmpty(),
    gistsUrl = response?.gistsUrl.orEmpty(),
    starredUrl = response?.starredUrl.orEmpty(),
    subscriptionsUrl = response?.subscriptionsUrl.orEmpty(),
    organizationsUrl = response?.organizationsUrl.orEmpty(),
    reposUrl = response?.reposUrl.orEmpty(),
    eventsUrl = response?.eventsUrl.orEmpty(),
    receivedEventsUrl = response?.receivedEventsUrl.orEmpty(),
    type = response?.type.orEmpty(),
    siteAdmin = response?.siteAdmin ?: false,
    name = response?.name.orEmpty(),
    company = response?.company.orEmpty(),
    blog = response?.blog.orEmpty(),
    location = response?.location.orEmpty(),
    email = response?.email.orEmpty(),
    hireable = response?.hireable ?: false,
    bio = response?.bio.orEmpty(),
    twitterUsername = response?.twitterUsername.orEmpty(),
    publicRepos = response?.publicRepos ?: 0,
    publicGists = response?.publicGists ?: 0,
    followers = response?.followers ?: 0,
    following = response?.following ?: 0,
    createdAt = response?.createdAt.orEmpty(),
    updatedAt = response?.updatedAt.orEmpty()
)
