package com.kturker.multiplatform.feature.monetization.presentation.ads

import com.kturker.multiplatform.core.navigation.FeatureEntry
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.feature.monetization.contract.AdsDemoDestination
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

// Lists the ads demo on a blank app's start screen (GuideScreen).
@Single(binds = [FeatureEntry::class])
@Named("AdsDemoEntry")
internal class AdsDemoEntry : FeatureEntry {
    override val title: String = "Ad examples"
    override val destination: NavigationCommand.Destination = AdsDemoDestination
}
