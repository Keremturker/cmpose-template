package com.kturker.multiplatform.core.purchases.di

import com.kturker.multiplatform.core.purchases.FirstPaywallStore
import com.kturker.multiplatform.core.purchases.getPlatformFirstPaywallStore
import org.koin.core.annotation.Single

@Single
fun provideFirstPaywallStore(): FirstPaywallStore = getPlatformFirstPaywallStore()
