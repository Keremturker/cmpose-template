package com.kturker.multiplatform.core.navigation

/**
 * A path a link can open (`<scheme>://<pattern>`): segments separated by "/", a `{name}` segment is a
 * parameter. Register it like a FeatureEntry: `@Single(binds = [DeepLinkRoute::class])`. Return null from
 * [destination] when an argument is not valid: the app then just opens.
 */
interface DeepLinkRoute {
    val pattern: String

    fun destination(arguments: Map<String, String>): NavigationCommand.Destination?
}
