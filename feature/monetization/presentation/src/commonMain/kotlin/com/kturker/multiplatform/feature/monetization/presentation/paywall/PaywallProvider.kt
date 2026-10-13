package com.kturker.multiplatform.feature.monetization.presentation.paywall

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kturker.multiplatform.core.navigation.NavGraphProvider
import com.kturker.multiplatform.feature.monetization.contract.PaywallDestination
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [NavGraphProvider::class])
@Named("PaywallProvider")
internal class PaywallProvider : NavGraphProvider {
    override fun registerGraph(provider: NavGraphBuilder) {
        provider.apply {
            composable<PaywallDestination> {
                PaywallScreenRoute()
            }
        }
    }
}
