package com.kturker.multiplatform.feature.account.presentation

import com.kturker.multiplatform.core.auth.AuthFailure
import com.kturker.multiplatform.core.auth.AuthResult
import com.kturker.multiplatform.core.auth.AuthState
import com.kturker.multiplatform.feature.account.presentation.ui.UiError
import com.kturker.multiplatform.feature.account.presentation.ui.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val auth = FakeAuthService(initial = AuthState.SignedOut)
    private val profileSync = FakeProfileSync()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = LoginViewModel(auth, profileSync).apply {
        onEmailChange("me@example.com")
        onPasswordChange("secret1")
    }

    @Test
    fun loginWrongPasswordShowsInvalidCredentials() = runTest {
        auth.emailSignIn[0] = AuthResult.Failure(AuthFailure.INVALID_CREDENTIALS)
        val viewModel = viewModel()

        viewModel.signIn()
        advanceUntilIdle()

        assertEquals(listOf("signInWithEmail:me@example.com"), auth.log)
        assertEquals(UiError.INVALID_CREDENTIALS, viewModel.state.value.error)
        assertFalse(viewModel.state.value.busy)
        assertEquals(0, profileSync.calls)
    }

    @Test
    fun googleCancelledLeavesNoError() = runTest {
        auth.google[0] = AuthResult.Cancelled
        val viewModel = viewModel()

        viewModel.signInWithGoogle()
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.busy)
        assertEquals(0, profileSync.calls)
    }

    @Test
    fun signUpErrorsMapToTheirMessages() = runTest {
        val viewModel = viewModel()

        auth.emailSignUp[0] = AuthResult.Failure(AuthFailure.EMAIL_IN_USE)
        viewModel.signUp()
        advanceUntilIdle()
        assertEquals(UiError.EMAIL_IN_USE, viewModel.state.value.error)

        auth.emailSignUp[0] = AuthResult.Failure(AuthFailure.WEAK_PASSWORD)
        viewModel.signUp()
        advanceUntilIdle()
        assertEquals(UiError.WEAK_PASSWORD, viewModel.state.value.error)

        auth.emailSignUp[0] = AuthResult.Failure(AuthFailure.NETWORK)
        viewModel.signUp()
        advanceUntilIdle()
        assertEquals(UiError.NETWORK, viewModel.state.value.error)
    }

    @Test
    fun appleUnavailableShowsAnErrorAndARetrySignsIn() = runTest {
        auth.apple[0] = AuthResult.Failure(AuthFailure.UNAVAILABLE)
        val viewModel = viewModel()

        viewModel.signInWithApple()
        advanceUntilIdle()
        assertEquals(UiError.UNKNOWN, viewModel.state.value.error)

        auth.apple[0] = AuthResult.Success
        viewModel.signInWithApple()
        advanceUntilIdle()
        assertNull(viewModel.state.value.error)
        assertEquals(AuthState.SignedIn("u1", "me@example.com"), auth.state.value)
    }

    @Test
    fun aSuccessfulSignInSyncsTheProfileOnce() = runTest {
        val viewModel = viewModel()

        viewModel.signInWithGoogle()
        advanceUntilIdle()

        assertEquals(1, profileSync.calls)
    }

    // No google-services.json / GoogleService-Info.plist: the screen says so, and no method reaches Firebase
    @Test
    fun unavailableFirebaseDisablesSignIn() = runTest {
        val auth = FakeAuthService(initial = AuthState.Unavailable)
        val viewModel = LoginViewModel(auth, profileSync)

        assertTrue(viewModel.state.value.firebaseUnavailable)
        assertFalse(viewModel.state.value.signInEnabled)

        viewModel.onEmailChange("me@example.com")
        viewModel.onPasswordChange("secret1")
        viewModel.signIn()
        viewModel.signUp()
        viewModel.signInWithGoogle()
        viewModel.signInWithApple()
        advanceUntilIdle()

        assertEquals(emptyList(), auth.log)
        assertFalse(viewModel.state.value.busy)
        assertNull(viewModel.state.value.error)
        assertEquals(0, profileSync.calls)
    }

    @Test
    fun aSignedOutAppWithFirebaseKeepsSignInEnabled() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.state.value.firebaseUnavailable)
        assertTrue(viewModel.state.value.signInEnabled)
    }
}
