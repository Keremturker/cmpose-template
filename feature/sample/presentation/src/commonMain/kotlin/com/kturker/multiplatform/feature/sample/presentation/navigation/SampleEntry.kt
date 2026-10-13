package com.kturker.multiplatform.feature.sample.presentation.navigation

import com.kturker.multiplatform.core.navigation.FeatureEntry
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.feature.sample.contract.SampleDestination
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

// Lists this module on a blank app's start screen (GuideScreen).
@Single(binds = [FeatureEntry::class])
@Named("SampleEntry")
internal class SampleEntry : FeatureEntry {
    override val title: String = "Sample"
    override val destination: NavigationCommand.Destination = SampleDestination
}
