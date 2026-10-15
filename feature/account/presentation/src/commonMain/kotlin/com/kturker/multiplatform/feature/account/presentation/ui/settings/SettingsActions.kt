package com.kturker.multiplatform.feature.account.presentation.ui.settings

internal interface SettingsActions {
    fun onBack()
    fun signOut()
    fun requestDelete()
    fun dismissDelete()
    fun confirmDelete()
    fun reauthWithEmail(
        email: String,
        password: String
    )
    fun reauthWithGoogle()
    fun reauthWithApple()
}
