package com.kturker.multiplatform.feature.account.presentation.ui.login

internal interface LoginActions {
    fun onEmailChange(email: String)
    fun onPasswordChange(password: String)
    fun signIn()
    fun signUp()
    fun signInWithGoogle()
    fun signInWithApple()
}
