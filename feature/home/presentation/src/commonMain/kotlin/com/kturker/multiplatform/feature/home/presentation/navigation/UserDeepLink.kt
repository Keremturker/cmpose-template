package com.kturker.multiplatform.feature.home.presentation.navigation

import com.kturker.multiplatform.core.navigation.DeepLinkRoute
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.feature.home.contract.HomeScreenDestination
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

// <scheme>://user/{username} opens that GitHub profile
@Single(binds = [DeepLinkRoute::class])
@Named("deeplink.user")
internal class UserDeepLink : DeepLinkRoute {
    override val pattern: String = "user/{username}"

    override fun destination(arguments: Map<String, String>): NavigationCommand.Destination? =
        arguments["username"]
            ?.takeIf { GITHUB_USERNAME.matches(it) }
            ?.let { HomeScreenDestination(githubUsername = it) }
}

private val GITHUB_USERNAME = Regex("^[A-Za-z0-9](?:[A-Za-z0-9-]{0,38})$")
