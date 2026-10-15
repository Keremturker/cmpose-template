package com.kturker.multiplatform.feature.account.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kturker.multiplatform.core.navigation.NavGraphProvider
import com.kturker.multiplatform.feature.account.contract.AccountSettingsDestination
import com.kturker.multiplatform.feature.account.contract.LoginDestination
import com.kturker.multiplatform.feature.account.presentation.ui.login.LoginRoute
import com.kturker.multiplatform.feature.account.presentation.ui.settings.SettingsRoute
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [NavGraphProvider::class])
@Named("AccountProvider")
internal class AccountProvider : NavGraphProvider {
    override fun registerGraph(provider: NavGraphBuilder) {
        provider.apply {
            composable<LoginDestination> { LoginRoute() }
            composable<AccountSettingsDestination> { SettingsRoute() }
        }
    }
}
