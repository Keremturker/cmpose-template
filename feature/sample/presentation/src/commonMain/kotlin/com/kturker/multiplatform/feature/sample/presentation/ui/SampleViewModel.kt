package com.kturker.multiplatform.feature.sample.presentation.ui

import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.core.navigation.NavigationManager
import com.kturker.multiplatform.core.presentation.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
internal class SampleViewModel(@Provided private val navigationManager: NavigationManager) :
    CoreViewModel(),
    SampleActions {

    private val _uiState = MutableStateFlow(SampleUiState())
    val uiState: StateFlow<SampleUiState> = _uiState.asStateFlow()

    override fun onNavigateUp() {
        navigationManager.navigate(NavigationCommand.NavigateUp)
    }
}
