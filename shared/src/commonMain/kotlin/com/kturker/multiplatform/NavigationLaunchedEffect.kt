package com.kturker.multiplatform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.core.navigation.NavigationManager

@Composable
internal fun NavigationLaunchedEffect(
    navigationManager: NavigationManager,
    navController: NavController
) {
    LaunchedEffect(key1 = Unit) {
        navigationManager.navigationCommandFlow.collect { command ->
            when (command) {
                NavigationCommand.NavigateUp -> {
                    navController.navigateUp()
                }

                is NavigationCommand.PopBackStackTo -> {
                    navController.popBackStack(route = command.to, inclusive = command.inclusive)
                }

                is NavigationCommand.NavigateTo -> {
                    navController.navigate(route = command.to) {
                        if (command.clearBackStack) {
                            // The destination becomes the only entry: Back leaves the app.
                            popUpTo(navController.graph.id) { inclusive = true }
                        } else if (!command.addToBackStack) {
                            // The destination replaces the current entry instead of stacking on it.
                            navController.currentDestination?.id?.let { popUpTo(it) { inclusive = true } }
                        }
                    }
                }

                is NavigationCommand.Destination -> {
                    navController.navigate(route = command)
                }
            }
        }
    }
}
