package com.kturker.multiplatform.feature.sharing.presentation.navigation

import com.kturker.multiplatform.core.navigation.DeepLinkRoute
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.feature.sharing.contract.InviteDestination
import com.kturker.multiplatform.feature.sharing.presentation.data.INVITE_PATH
import com.kturker.multiplatform.feature.sharing.presentation.data.isInviteToken
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

/**
 * `<scheme>://invite/<token>` opens the invite's preview. A token that is not base64url (empty, with a `/` even
 * percent-encoded, any other character) opens nothing: the app just starts.
 */
@Single(binds = [DeepLinkRoute::class])
@Named("SharingDeepLink")
internal class SharingDeepLink : DeepLinkRoute {
    override val pattern: String = "$INVITE_PATH/{$TOKEN}"

    override fun destination(arguments: Map<String, String>): NavigationCommand.Destination? =
        arguments[TOKEN]?.takeIf(::isInviteToken)?.let(::InviteDestination)

    private companion object {
        const val TOKEN = "token"
    }
}
