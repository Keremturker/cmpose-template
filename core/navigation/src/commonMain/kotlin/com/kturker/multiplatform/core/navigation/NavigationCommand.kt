package com.kturker.multiplatform.core.navigation

sealed interface NavigationCommand {
    interface Destination : NavigationCommand

    data class NavigateTo(
        val to: Destination,
        val clearBackStack: Boolean = false,
        val addToBackStack: Boolean = true
    ) : NavigationCommand

    data object NavigateUp : NavigationCommand

    data class PopBackStackTo(val to: Destination, val inclusive: Boolean = false) : NavigationCommand
}
