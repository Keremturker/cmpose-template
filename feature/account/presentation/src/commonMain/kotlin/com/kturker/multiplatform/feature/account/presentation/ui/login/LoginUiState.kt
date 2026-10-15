package com.kturker.multiplatform.feature.account.presentation.ui.login

import com.kturker.multiplatform.feature.account.presentation.ui.UiError

internal data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val busy: Boolean = false,
    val error: UiError? = null,
    /** The previous session ended on the server's 401, not by the user's choice; cleared by the next attempt. */
    val sessionEnded: Boolean = false,
    /** The app has no Firebase config: the screen says so and every sign-in method is off. */
    val firebaseUnavailable: Boolean = false
) {
    val signInEnabled: Boolean get() = !busy && !firebaseUnavailable
}
