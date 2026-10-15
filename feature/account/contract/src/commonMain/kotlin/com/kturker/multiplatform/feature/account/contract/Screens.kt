@file:Suppress("Filename")

package com.kturker.multiplatform.feature.account.contract

import com.kturker.multiplatform.core.navigation.NavigationCommand
import kotlinx.serialization.Serializable

/** Sign-in with email and password, Google or Apple; the app shell opens it while nobody is signed in. */
@Serializable
data object LoginDestination : NavigationCommand.Destination

/** The account's settings: sign-out and account deletion. */
@Serializable
data object AccountSettingsDestination : NavigationCommand.Destination
