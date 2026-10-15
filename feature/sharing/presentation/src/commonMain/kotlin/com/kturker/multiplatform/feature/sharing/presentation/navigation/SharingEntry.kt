package com.kturker.multiplatform.feature.sharing.presentation.navigation

import com.kturker.multiplatform.core.multilang.language.LanguageManager
import com.kturker.multiplatform.core.multilang.resource.sharing
import com.kturker.multiplatform.core.navigation.FeatureEntry
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.feature.sharing.contract.SharingConfig
import com.kturker.multiplatform.feature.sharing.contract.SpacesDestination
import org.koin.core.annotation.Named
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

// Lists the spaces on a blank app's start screen (GuideScreen).
@Single(binds = [FeatureEntry::class])
@Named("SharingEntry")
internal class SharingEntry(@Provided private val languageManager: LanguageManager) : FeatureEntry {
    // Read on every use: the title follows a language change in the app
    override val title: String get() = languageManager.currentResources.value.sharing.spacesTitle
    override val destination: NavigationCommand.Destination = SpacesDestination

    // An app without sharing has only its personal space: no list of spaces to open
    override val visible: Boolean = SharingConfig.ENABLED
}
