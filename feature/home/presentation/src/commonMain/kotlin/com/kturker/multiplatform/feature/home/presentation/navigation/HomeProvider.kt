package com.kturker.multiplatform.feature.home.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kturker.multiplatform.core.navigation.NavGraphProvider
import com.kturker.multiplatform.feature.home.contract.HomeScreenDestination
import com.kturker.multiplatform.feature.home.presentation.ui.HomeScreenRoute
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [NavGraphProvider::class])
@Named("HomeProvider")
internal class HomeProvider : NavGraphProvider {
    override fun registerGraph(provider: NavGraphBuilder) {
        provider.apply {
            composable<HomeScreenDestination> {
                HomeScreenRoute()
            }
        }
    }
}
