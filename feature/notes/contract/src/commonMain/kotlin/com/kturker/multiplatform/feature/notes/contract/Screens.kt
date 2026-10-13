@file:Suppress("Filename", "MatchingDeclarationName")

package com.kturker.multiplatform.feature.notes.contract

import com.kturker.multiplatform.core.navigation.NavigationCommand
import kotlinx.serialization.Serializable

@Serializable
data object NotesDestination : NavigationCommand.Destination
