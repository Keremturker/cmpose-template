package com.kturker.multiplatform.feature.notes.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kturker.multiplatform.core.navigation.NavGraphProvider
import com.kturker.multiplatform.feature.notes.contract.NotesDestination
import com.kturker.multiplatform.feature.notes.presentation.ui.NotesScreenRoute
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [NavGraphProvider::class])
@Named("NotesProvider")
internal class NotesProvider : NavGraphProvider {
    override fun registerGraph(provider: NavGraphBuilder) {
        provider.apply {
            composable<NotesDestination> {
                NotesScreenRoute()
            }
        }
    }
}
