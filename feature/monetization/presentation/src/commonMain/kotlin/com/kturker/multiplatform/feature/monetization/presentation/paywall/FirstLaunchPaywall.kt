package com.kturker.multiplatform.feature.monetization.presentation.paywall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kturker.multiplatform.core.navigation.NavigationManager
import com.kturker.multiplatform.core.purchases.FirstPaywallStore
import com.kturker.multiplatform.core.purchases.PurchasesManager
import com.kturker.multiplatform.feature.monetization.contract.PaywallDestination
import org.koin.compose.koinInject
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Opens the paywall once, the first time [ready] is true (a blank app: at launch; the showcase: when onboarding
 * is done and a user's Home opens), unless the user is premium or the premium state is not known yet. It can
 * always be closed (Not now). Call it once in MainScreen. The flag stays on the device (PAYWALL_FIRST_SHOWN_KEY).
 */
@Composable
fun FirstLaunchPaywall(ready: Boolean = true) {
    val gate = koinInject<FirstPaywallGate>()
    val purchasesManager = koinInject<PurchasesManager>()
    val isPremium by purchasesManager.isPremium.collectAsState()

    // Asks RevenueCat once at launch, so a returning premium user is known before the paywall would open
    LaunchedEffect(purchasesManager) { purchasesManager.refresh() }
    LaunchedEffect(ready, isPremium) { gate.openIfDue(ready = ready) }
}

@Single
internal class FirstPaywallGate(
    @Provided private val purchasesManager: PurchasesManager,
    @Provided private val store: FirstPaywallStore,
    @Provided private val navigationManager: NavigationManager
) {
    // Opens the paywall when it is due and remembers it; true when it opened
    fun openIfDue(ready: Boolean): Boolean {
        val due = shouldOpenFirstPaywall(
            ready = ready,
            isAvailable = purchasesManager.isAvailable,
            alreadyShown = store.isShown(),
            isPremium = purchasesManager.isPremium.value
        )
        if (due) {
            store.markShown()
            navigationManager.navigate(PaywallDestination)
        }
        return due
    }
}

// Due once: purchases work (not a release build with a Test Store key), the app is ready, it never opened
// before and the user is known not to be premium (null: RevenueCat has not answered yet, so wait)
internal fun shouldOpenFirstPaywall(
    ready: Boolean,
    isAvailable: Boolean,
    alreadyShown: Boolean,
    isPremium: Boolean?
): Boolean = ready && isAvailable && !alreadyShown && isPremium == false
