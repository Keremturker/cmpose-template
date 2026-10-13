package com.kturker.multiplatform.feature.monetization.presentation.paywall

import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.core.navigation.NavigationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class RecordingNavigationManager : NavigationManager {
    val commands = mutableListOf<NavigationCommand>()
    override val navigationCommandFlow: Flow<NavigationCommand> = emptyFlow()

    override fun navigate(navigationCommand: NavigationCommand) {
        commands += navigationCommand
    }
}
