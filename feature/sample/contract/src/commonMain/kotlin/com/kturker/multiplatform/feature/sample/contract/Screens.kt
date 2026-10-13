@file:Suppress("Filename", "MatchingDeclarationName")

package com.kturker.multiplatform.feature.sample.contract

import com.kturker.multiplatform.core.navigation.NavigationCommand
import kotlinx.serialization.Serializable

@Serializable
data object SampleDestination : NavigationCommand.Destination
