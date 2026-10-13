package com.kturker.multiplatform.feature.monetization.presentation.paywall

import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.core.purchases.FirstPaywallStore
import com.kturker.multiplatform.feature.monetization.contract.PaywallDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FirstPaywallGateTest {

    private val navigationManager = RecordingNavigationManager()
    private val store = InMemoryFirstPaywallStore()

    private fun gate(purchasesManager: FakePurchasesManager) =
        FirstPaywallGate(purchasesManager = purchasesManager, store = store, navigationManager = navigationManager)

    @Test
    fun opensOnceWhenDue() {
        val gate = gate(FakePurchasesManager(premium = false))

        assertTrue(gate.openIfDue(ready = true))
        assertFalse(gate.openIfDue(ready = true))

        assertEquals(listOf<NavigationCommand>(PaywallDestination), navigationManager.commands)
        assertTrue(store.isShown())
    }

    @Test
    fun waitsUntilTheAppIsReady() {
        assertFalse(gate(FakePurchasesManager(premium = false)).openIfDue(ready = false))

        assertEquals(emptyList<NavigationCommand>(), navigationManager.commands)
        assertFalse(store.isShown())
    }

    @Test
    fun waitsWhilePremiumIsUnknown() {
        val purchases = FakePurchasesManager(premium = null)
        val gate = gate(purchases)

        assertFalse(gate.openIfDue(ready = true))
        assertFalse(store.isShown())

        purchases.premiumState.value = false
        assertTrue(gate.openIfDue(ready = true))
    }

    @Test
    fun neverForPremium() {
        assertFalse(gate(FakePurchasesManager(premium = true)).openIfDue(ready = true))
        assertFalse(store.isShown())
    }

    @Test
    fun neverWithoutRevenueCat() {
        assertFalse(gate(FakePurchasesManager(isAvailable = false, premium = false)).openIfDue(ready = true))
        assertEquals(emptyList<NavigationCommand>(), navigationManager.commands)
    }

    @Test
    fun notAfterAnEarlierLaunchShowedIt() {
        store.markShown()

        assertFalse(gate(FakePurchasesManager(premium = false)).openIfDue(ready = true))
    }

    @Test
    fun dueOnlyWhenEverythingAgrees() {
        assertTrue(shouldOpenFirstPaywall(ready = true, isAvailable = true, alreadyShown = false, isPremium = false))
        assertFalse(shouldOpenFirstPaywall(ready = false, isAvailable = true, alreadyShown = false, isPremium = false))
        assertFalse(shouldOpenFirstPaywall(ready = true, isAvailable = false, alreadyShown = false, isPremium = false))
        assertFalse(shouldOpenFirstPaywall(ready = true, isAvailable = true, alreadyShown = true, isPremium = false))
        assertFalse(shouldOpenFirstPaywall(ready = true, isAvailable = true, alreadyShown = false, isPremium = null))
        assertFalse(shouldOpenFirstPaywall(ready = true, isAvailable = true, alreadyShown = false, isPremium = true))
    }
}

private class InMemoryFirstPaywallStore : FirstPaywallStore {
    private var shown = false

    override fun isShown(): Boolean = shown

    override fun markShown() {
        shown = true
    }
}
