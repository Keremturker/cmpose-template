@file:Suppress("Filename")

package com.kturker.multiplatform

import com.kturker.multiplatform.core.auth.IosSignInBridge
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/** Hands the Swift sign-in bridge to Koin; call once, right after startKoinAtLaunch(). */
fun registerSignInBridge(signIn: IosSignInBridge) {
    loadKoinModules(module { single<IosSignInBridge> { signIn } })
}
