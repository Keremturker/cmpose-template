package com.kturker.multiplatform.feature.sharing.presentation.ui

import androidx.compose.runtime.Composable
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.auth.AuthService
import com.kturker.multiplatform.core.auth.SessionTokens
import com.kturker.multiplatform.core.auth.SignOutReason
import com.kturker.multiplatform.core.auth.signOutAndForget
import com.kturker.multiplatform.core.designsystem.language.LocalStringResources
import com.kturker.multiplatform.core.domain.DataError
import com.kturker.multiplatform.core.multilang.resource.SharingStrings
import com.kturker.multiplatform.core.multilang.resource.sharing
import com.kturker.multiplatform.feature.sharing.contract.SelectedSpace

/** What the spaces and members screens tell the user; each one has a message in [SharingStrings]. */
internal enum class SharingError { NO_ACCESS, NETWORK, UNKNOWN }

internal const val HTTP_UNAUTHORIZED = 401
internal const val HTTP_NOT_FOUND = 404

internal val DataError.httpCode: Int? get() = (this as? DataError.Http)?.code

/**
 * The account rule for a failed backend call, shared by the sharing screens. A 401 that survived the token
 * refresh ends the session (signed out, cached token dropped; the sign-in screen explains) and shows nothing
 * here. A 404 means the space was deleted or the user was removed from it: the selection is refreshed, so the
 * app's screens fall back to the personal space.
 */
internal class BackendFailures(
    private val auth: AuthService,
    private val tokens: SessionTokens,
    private val selectedSpace: SelectedSpace
) {
    suspend fun handle(error: DataError): SharingError? = when {
        error.httpCode == HTTP_UNAUTHORIZED -> {
            auth.signOutAndForget(tokens, SignOutReason.SESSION_ENDED)
            null
        }

        error.httpCode == HTTP_NOT_FOUND -> noAccess()

        error is DataError.Network -> SharingError.NETWORK

        else -> SharingError.UNKNOWN
    }

    suspend fun noAccess(): SharingError {
        selectedSpace.refresh()
        return SharingError.NO_ACCESS
    }
}

internal fun SharingError.message(strings: SharingStrings): String = when (this) {
    SharingError.NO_ACCESS -> strings.errorNoAccess
    SharingError.NETWORK -> strings.errorNetwork
    SharingError.UNKNOWN -> strings.errorUnknown
}

internal fun Role.label(strings: SharingStrings): String = when (this) {
    Role.OWNER -> strings.roleOwner
    Role.EDITOR -> strings.roleEditor
    Role.VIEWER -> strings.roleViewer
}

@Composable
internal fun sharingErrorText(error: SharingError): String =
    error.message(LocalStringResources.current.sharing)
