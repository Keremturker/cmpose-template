package com.kturker.multiplatform.feature.home.presentation.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.kturker.multiplatform.core.domain.ConnectivityMonitor
import com.kturker.multiplatform.core.domain.DataError
import com.kturker.multiplatform.core.domain.onError
import com.kturker.multiplatform.core.domain.onLoading
import com.kturker.multiplatform.core.domain.onSuccess
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.core.navigation.NavigationManager
import com.kturker.multiplatform.core.presentation.CoreViewModel
import com.kturker.multiplatform.core.review.ReviewPrompter
import com.kturker.multiplatform.feature.home.contract.HomeScreenDestination
import com.kturker.multiplatform.feature.home.domain.model.FavoriteUserModel
import com.kturker.multiplatform.feature.home.domain.usecase.GetGithubUserDetailUseCase
import com.kturker.multiplatform.feature.home.domain.usecase.ObserveIsFavoriteUseCase
import com.kturker.multiplatform.feature.home.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
internal class HomeViewModel(
    private val savedStateHandle: SavedStateHandle,
    @Provided private val getGithubUserDetail: GetGithubUserDetailUseCase,
    @Provided private val observeIsFavorite: ObserveIsFavoriteUseCase,
    @Provided private val toggleFavorite: ToggleFavoriteUseCase,
    @Provided private val navigationManager: NavigationManager,
    @Provided private val reviewPrompter: ReviewPrompter,
    @Provided private val connectivityMonitor: ConnectivityMonitor
) : CoreViewModel(),
    HomeActions {

    // The route's argument, read by its name: toRoute<HomeScreenDestination>() decodes it through an
    // Android Bundle, which the JVM unit tests (testAndroidHostTest) do not have
    private val githubUsername: String =
        checkNotNull(savedStateHandle[HomeScreenDestination::githubUsername.name]) { "githubUsername missing" }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState>
        get() = _uiState

    // The running load: a retry cancels it before loading again
    private var loadJob: Job? = null

    init {
        loadUser()
        // Back online after a connection error: load again (an HTTP or parsing error is not retried)
        connectivityMonitor.reconnections()
            .onEach { if (_uiState.value.error == DataError.Network) onRetry() }
            .launchIn(viewModelScope)
        observeIsFavorite(login = githubUsername)
            .onEach { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
            .launchIn(viewModelScope)
    }

    override fun onNavigateUp() {
        navigationManager.navigate(NavigationCommand.NavigateUp)
    }

    override fun onRetry() {
        loadUser()
    }

    override fun onFavoriteClick() {
        val user = _uiState.value.user ?: return
        val favorite = FavoriteUserModel(
            login = githubUsername,
            avatarUrl = user.avatarUrl.orEmpty(),
            name = user.name?.takeIf { it.isNotBlank() }
        )
        val isFavorite = _uiState.value.isFavorite
        // Saving someone is a moment the user wanted: it counts towards asking for a review
        if (!isFavorite) reviewPrompter.recordPositiveEvent()
        viewModelScope.launch { toggleFavorite(user = favorite, isFavorite = isFavorite) }
    }

    private fun loadUser() {
        loadJob?.cancel()
        _uiState.update { it.copy(error = null) }
        loadJob = safeFlowApiCall {
            getGithubUserDetail.invoke(username = githubUsername)
        }.onSuccess { response ->
            _uiState.update { it.copy(user = response) }
        }.onError { error ->
            _uiState.update { it.copy(error = error) }
        }.onLoading { isLoading ->
            _uiState.update { it.copy(isLoading = isLoading) }
        }.launchIn(viewModelScope)
    }
}
