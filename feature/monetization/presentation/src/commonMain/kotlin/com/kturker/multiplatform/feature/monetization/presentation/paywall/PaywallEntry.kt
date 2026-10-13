package com.kturker.multiplatform.feature.monetization.presentation.paywall

import com.kturker.multiplatform.core.navigation.FeatureEntry
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.feature.monetization.contract.PaywallDestination
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

// Lists the paywall on a blank app's start screen (GuideScreen).
@Single(binds = [FeatureEntry::class])
@Named("PaywallEntry")
internal class PaywallEntry : FeatureEntry {
    override val title: String = "Premium"
    override val destination: NavigationCommand.Destination = PaywallDestination
}
