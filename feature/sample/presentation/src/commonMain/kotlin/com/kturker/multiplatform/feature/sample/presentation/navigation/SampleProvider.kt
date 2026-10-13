package com.kturker.multiplatform.feature.sample.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kturker.multiplatform.core.navigation.NavGraphProvider
import com.kturker.multiplatform.feature.sample.contract.SampleDestination
import com.kturker.multiplatform.feature.sample.presentation.ui.SampleScreenRoute
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [NavGraphProvider::class])
@Named("SampleProvider")
internal class SampleProvider : NavGraphProvider {
    override fun registerGraph(provider: NavGraphBuilder) {
        provider.apply {
            composable<SampleDestination> {
                SampleScreenRoute()
            }
        }
    }
}
