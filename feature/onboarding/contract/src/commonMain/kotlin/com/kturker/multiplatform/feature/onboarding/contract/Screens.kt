@file:Suppress("Filename", "MatchingDeclarationName")

package com.kturker.multiplatform.feature.onboarding.contract

import com.kturker.multiplatform.core.navigation.NavigationCommand
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingDestination : NavigationCommand.Destination

@Serializable
data object SettingsDestination : NavigationCommand.Destination
