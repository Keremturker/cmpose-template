package com.kturker.multiplatform.feature.sharing.contract

import com.kturker.multiplatform.core.navigation.NavigationCommand
import kotlinx.serialization.Serializable

/** The user's spaces: switch the selected one, create, rename, delete or leave one. */
@Serializable
data object SpacesDestination : NavigationCommand.Destination

/** A space's members; its owner changes roles, removes members and shares invite links. */
@Serializable
data class MembersDestination(val spaceId: String) : NavigationCommand.Destination

/** An invite link's preview and acceptance; `<scheme>://invite/<token>` opens it. */
@Serializable
data class InviteDestination(val token: String) : NavigationCommand.Destination

/** Whether the app uses sharing; when false the space switcher and Share are hidden (the code step sets it). */
object SharingConfig {
    const val ENABLED = true
}
