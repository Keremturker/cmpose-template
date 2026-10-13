package com.kturker.multiplatform.feature.notes.presentation.navigation

import com.kturker.multiplatform.core.navigation.FeatureEntry
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.feature.notes.contract.NotesDestination
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

// Lists this module on a blank app's start screen (GuideScreen).
@Single(binds = [FeatureEntry::class])
@Named("NotesEntry")
internal class NotesEntry : FeatureEntry {
    override val title: String = "Notes"
    override val destination: NavigationCommand.Destination = NotesDestination
}
