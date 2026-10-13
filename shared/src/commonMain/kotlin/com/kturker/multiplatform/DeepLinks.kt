@file:Suppress("Filename")

package com.kturker.multiplatform

import com.kturker.multiplatform.core.navigation.DeepLinkHandler
import org.koin.mp.KoinPlatform.getKoin

// Called by Android's MainActivity and iOS's onOpenURL (DeepLinksKt.handleDeepLink) with the link's text.
// Koin is already started there (Application, iOSApp.init).
fun handleDeepLink(uri: String): Boolean = getKoin().get<DeepLinkHandler>().handle(uri)
