package com.kturker.multiplatform.feature.account.presentation.ui

/** What [SignInMethods] calls back; [onCreateAccount] only on the sign-in screen, not the re-sign-in. */
internal class SignInCallbacks(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onSignIn: () -> Unit,
    val onGoogle: () -> Unit,
    val onApple: () -> Unit,
    val onCreateAccount: (() -> Unit)? = null
)
