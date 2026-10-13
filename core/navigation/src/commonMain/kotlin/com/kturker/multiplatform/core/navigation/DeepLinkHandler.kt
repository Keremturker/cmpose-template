package com.kturker.multiplatform.core.navigation

import co.touchlab.kermit.Logger
import org.koin.core.annotation.Single
import org.koin.mp.KoinPlatform.getKoin

private val log = Logger.withTag("DeepLink")

/**
 * Opens the screen a link points to: Android's MainActivity and iOS's onOpenURL call it through shared's
 * handleDeepLink. Returns false for a link this app does not know (it then just opens).
 */
@Single
class DeepLinkHandler(
    private val navigationManager: NavigationManager,
    private val routes: () -> List<DeepLinkRoute> = { getKoin().getAll<DeepLinkRoute>().sortedBy { it.pattern } }
) {
    fun handle(uri: String): Boolean {
        // Another app's link: not ours. "<scheme>://" alone: the app just opens at its start screen.
        val segments = linkPath(uri = uri, scheme = DEEP_LINK_SCHEME, appLinkHost = APP_LINK_HOST) ?: return false
        if (segments.isEmpty()) return true
        val all = routes()
        if (all.groupBy { it.pattern }.any { it.value.size > 1 }) log.w { "Two deep link routes for one path" }
        val destination = parseDeepLink(uri = uri, scheme = DEEP_LINK_SCHEME, appLinkHost = APP_LINK_HOST, routes = all)
        if (destination == null) {
            log.w { "Unknown deep link path" }
            return false
        }
        log.d { "Deep link: ${destination::class.simpleName}" }
        navigationManager.navigate(destination)
        return true
    }
}
