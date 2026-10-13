package com.kturker.multiplatform.feature.onboarding.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kturker.multiplatform.core.navigation.NavGraphProvider
import com.kturker.multiplatform.feature.onboarding.contract.OnboardingDestination
import com.kturker.multiplatform.feature.onboarding.contract.SettingsDestination
import com.kturker.multiplatform.feature.onboarding.presentation.ui.main.OnboardingScreenRoute
import com.kturker.multiplatform.feature.onboarding.presentation.ui.settings.SettingsScreenRoute
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [NavGraphProvider::class])
@Named("OnboardingProvider")
internal class OnboardingProvider : NavGraphProvider {
    override fun registerGraph(provider: NavGraphBuilder) {
        provider.apply {
            composable<OnboardingDestination> {
                OnboardingScreenRoute()
            }

            composable<SettingsDestination> {
                SettingsScreenRoute()
            }
        }
    }
}
