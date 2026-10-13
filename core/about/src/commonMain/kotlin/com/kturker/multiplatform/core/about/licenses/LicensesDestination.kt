package com.kturker.multiplatform.core.about.licenses

import com.kturker.multiplatform.core.navigation.NavigationCommand
import kotlinx.serialization.Serializable

// The title comes with the route: core/about has no string resources of its own
@Serializable
data class LicensesDestination(val title: String) : NavigationCommand.Destination
