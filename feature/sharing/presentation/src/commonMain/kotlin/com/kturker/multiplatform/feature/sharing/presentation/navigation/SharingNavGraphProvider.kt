package com.kturker.multiplatform.feature.sharing.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kturker.multiplatform.core.navigation.NavGraphProvider
import com.kturker.multiplatform.feature.sharing.contract.InviteDestination
import com.kturker.multiplatform.feature.sharing.contract.MembersDestination
import com.kturker.multiplatform.feature.sharing.contract.SpacesDestination
import com.kturker.multiplatform.feature.sharing.presentation.ui.invite.InviteRoute
import com.kturker.multiplatform.feature.sharing.presentation.ui.members.MembersRoute
import com.kturker.multiplatform.feature.sharing.presentation.ui.spaces.SpacesRoute
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

// The routes' arguments (spaceId, token) reach the ViewModels through their SavedStateHandle
@Single(binds = [NavGraphProvider::class])
@Named("SharingNavGraphProvider")
internal class SharingNavGraphProvider : NavGraphProvider {
    override fun registerGraph(provider: NavGraphBuilder) {
        provider.apply {
            composable<SpacesDestination> { SpacesRoute() }
            composable<MembersDestination> { MembersRoute() }
            composable<InviteDestination> { InviteRoute() }
        }
    }
}
