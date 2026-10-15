package com.kturker.multiplatform.core.auth

import kotlinx.coroutines.flow.MutableStateFlow

/** Why the user was signed out: by their own choice, or because the server no longer accepted the session. */
enum class SignOutReason { USER, SESSION_ENDED }

/**
 * A one-shot "your session ended" flag: posted when a 401 the token refresh could not cure signs the user
 * out, taken once by the sign-in screen that follows. A sign-out the user chose posts nothing.
 */
class SessionNotice {
    private val pending = MutableStateFlow(false)

    fun post() {
        pending.value = true
    }

    /** True once after [post]; every later call is false until the next [post]. */
    fun take(): Boolean = pending.compareAndSet(expect = true, update = false)
}
