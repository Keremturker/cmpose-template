package com.kturker.multiplatform.core.navigation

/**
 * A feature listed on a blank app's start screen (GuideScreen): its title and the destination it
 * opens. Each feature module registers one with `@Single(binds = [FeatureEntry::class])`.
 */
interface FeatureEntry {
    val title: String
    val destination: NavigationCommand.Destination
}
