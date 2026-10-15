package com.kturker.multiplatform.feature.sharing.presentation.ui.invite

import com.kturker.multiplatform.core.api.InvitePreview
import com.kturker.multiplatform.core.api.InviteState

/** Why an invite cannot be accepted; each one has a message in SharingStrings. */
internal enum class InviteMessage { USED, EXPIRED, NOT_FOUND, NETWORK, UNKNOWN }

internal data class InviteUiState(
    val loading: Boolean = true,
    val preview: InvitePreview? = null,
    val busy: Boolean = false,
    val message: InviteMessage? = null
) {
    /** A valid invite: Accept and Not now; otherwise only the message and Close. */
    val canAccept: Boolean get() = preview?.state == InviteState.VALID && message == null

    /** No connection or a server error: the preview can be asked for again. */
    val canRetry: Boolean get() = message == InviteMessage.NETWORK || message == InviteMessage.UNKNOWN
}
