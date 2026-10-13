package com.kturker.multiplatform.core.navigation

import kotlinx.coroutines.flow.Flow

interface NavigationManager {

    val navigationCommandFlow: Flow<NavigationCommand>

    fun navigate(navigationCommand: NavigationCommand)
}
