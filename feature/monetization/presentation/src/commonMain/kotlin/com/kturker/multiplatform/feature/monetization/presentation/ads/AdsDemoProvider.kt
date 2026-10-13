package com.kturker.multiplatform.feature.monetization.presentation.ads

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kturker.multiplatform.core.navigation.NavGraphProvider
import com.kturker.multiplatform.feature.monetization.contract.AdsDemoDestination
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [NavGraphProvider::class])
@Named("AdsDemoProvider")
internal class AdsDemoProvider : NavGraphProvider {
    override fun registerGraph(provider: NavGraphBuilder) {
        provider.apply {
            composable<AdsDemoDestination> {
                AdsDemoScreenRoute()
            }
        }
    }
}
