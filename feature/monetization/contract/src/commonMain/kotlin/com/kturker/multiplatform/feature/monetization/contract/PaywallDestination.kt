@file:Suppress("Filename", "MatchingDeclarationName")

package com.kturker.multiplatform.feature.monetization.contract

import com.kturker.multiplatform.core.navigation.NavigationCommand
import kotlinx.serialization.Serializable

@Serializable
data object PaywallDestination : NavigationCommand.Destination
