package com.kturker.multiplatform.core.auth

sealed interface AuthState {
    /** Firebase has not reported the restored session yet. */
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val uid: String, val email: String?) : AuthState

    /** The app has no Firebase config (google-services.json / GoogleService-Info.plist): sign-in is off. */
    data object Unavailable : AuthState
}
