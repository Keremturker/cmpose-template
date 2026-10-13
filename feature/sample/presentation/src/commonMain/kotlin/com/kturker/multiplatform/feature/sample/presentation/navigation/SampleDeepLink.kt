package com.kturker.multiplatform.feature.sample.presentation.navigation

import com.kturker.multiplatform.core.navigation.DeepLinkRoute
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.feature.sample.contract.SampleDestination
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

// <scheme>://sample opens this module. A generated module gets its own name as the path; add a
// "{id}" segment and read it from arguments to open one item.
@Single(binds = [DeepLinkRoute::class])
@Named("SampleDeepLink")
internal class SampleDeepLink : DeepLinkRoute {
    override val pattern: String = "sample"

    override fun destination(arguments: Map<String, String>): NavigationCommand.Destination =
        SampleDestination
}
