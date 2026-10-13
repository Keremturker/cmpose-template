package com.kturker.multiplatform.feature.sample.presentation.ui

import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.core.navigation.NavigationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.test.Test
import kotlin.test.assertEquals

class SampleViewModelTest {

    private val navigationManager = RecordingNavigationManager()

    @Test
    fun showsTheModuleTitle() {
        assertEquals(SampleUiState(title = "Sample"), SampleViewModel(navigationManager).uiState.value)
    }

    @Test
    fun backNavigatesUp() {
        SampleViewModel(navigationManager).onNavigateUp()

        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateUp), navigationManager.commands)
    }
}

private class RecordingNavigationManager : NavigationManager {
    val commands = mutableListOf<NavigationCommand>()
    override val navigationCommandFlow: Flow<NavigationCommand> = emptyFlow()

    override fun navigate(navigationCommand: NavigationCommand) {
        commands += navigationCommand
    }
}
