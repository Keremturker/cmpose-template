package com.kturker.multiplatform.feature.account.presentation.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kturker.multiplatform.core.designsystem.language.LocalStringResources
import com.kturker.multiplatform.core.multilang.resource.account
import com.kturker.multiplatform.feature.account.presentation.ui.SignInCallbacks
import com.kturker.multiplatform.feature.account.presentation.ui.SignInMethods
import com.kturker.multiplatform.feature.account.presentation.ui.uiErrorText
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LoginRoute(viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginScreen(state = state, actions = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(
    state: LoginUiState,
    actions: LoginActions
) {
    val strings = LocalStringResources.current.account
    Scaffold(topBar = { TopAppBar(title = { Text(strings.signIn) }) }) { padding ->
        Column(
            // The keyboard does not resize the window (edge to edge, and iOS): shrink the column so the
            // buttons below the text fields can still be scrolled into view while typing
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            if (state.firebaseUnavailable) {
                Text(strings.firebaseUnavailable, color = MaterialTheme.colorScheme.error)
            }
            if (state.sessionEnded) {
                Text(strings.sessionEnded, color = MaterialTheme.colorScheme.error)
            }
            SignInMethods(
                email = state.email,
                password = state.password,
                enabled = state.signInEnabled,
                callbacks = SignInCallbacks(
                    onEmailChange = actions::onEmailChange,
                    onPasswordChange = actions::onPasswordChange,
                    onSignIn = actions::signIn,
                    onGoogle = actions::signInWithGoogle,
                    onApple = actions::signInWithApple,
                    onCreateAccount = actions::signUp
                )
            )
            state.error?.let { Text(uiErrorText(it), color = MaterialTheme.colorScheme.error) }
        }
    }
}
