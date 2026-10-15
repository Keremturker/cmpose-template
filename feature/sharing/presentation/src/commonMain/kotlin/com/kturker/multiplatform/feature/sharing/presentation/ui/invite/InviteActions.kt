package com.kturker.multiplatform.feature.sharing.presentation.ui.invite

internal interface InviteActions {
    fun accept()

    /** Not now, or Close: back to where the user came from, the invite left unused. */
    fun close()
    fun retry()
}
