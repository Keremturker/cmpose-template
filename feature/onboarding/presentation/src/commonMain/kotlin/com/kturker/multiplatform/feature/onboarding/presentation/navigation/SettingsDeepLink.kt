package com.kturker.multiplatform.feature.onboarding.presentation.navigation

import com.kturker.multiplatform.core.navigation.DeepLinkRoute
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.feature.onboarding.contract.SettingsDestination
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [DeepLinkRoute::class])
@Named("deeplink.settings")
internal class SettingsDeepLink : DeepLinkRoute {
    override val pattern: String = "settings"

    override fun destination(arguments: Map<String, String>): NavigationCommand.Destination =
        SettingsDestination
}
